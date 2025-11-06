package com.project.reservation_service.service.impl;

import com.project.reservation_service.client.NotificationServiceClient;
import com.project.reservation_service.dto.*;
import com.project.reservation_service.enums.NotificationType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.reservation_service.client.SeatServiceClient;
import com.project.reservation_service.client.ShowServiceClient;
import com.project.reservation_service.client.UserServiceClient;
import com.project.reservation_service.entity.Reservation;
import com.project.reservation_service.enums.ReservationStatus;
import com.project.reservation_service.exception.*;
import com.project.reservation_service.repository.ReservationRepository;
import com.project.reservation_service.service.ReservationService;

import static com.project.reservation_service.constant.ExceptionMessages.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserServiceClient userServiceClient;
    private final ShowServiceClient showServiceClient;
    private final SeatServiceClient seatServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Autowired
    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserServiceClient userServiceClient,
            ShowServiceClient showServiceClient,
            SeatServiceClient seatServiceClient,
            NotificationServiceClient notificationServiceClient
            ) {
          this.reservationRepository = reservationRepository;
          this.userServiceClient = userServiceClient;
          this.showServiceClient = showServiceClient;
          this.seatServiceClient = seatServiceClient;
          this.notificationServiceClient = notificationServiceClient;
    }

    @Transactional
    @Override
    public Reservation createReservation(ReservationRequestDto reservationRequestDto, Long userId) {
        UserDto user = userServiceClient.getUserById(userId);

        ShowDto show = showServiceClient.getShowById(reservationRequestDto.getShowId());

        // 3. Lock seats
        List<Long> seatIds = reservationRequestDto.getSeatIdsToReserve();
        try {
            seatServiceClient.lockSeats(seatIds);

            // 4. Validate amount
            double totalPrice = seatIds.stream()
                    .mapToDouble(id -> seatServiceClient.getSeatById(id).getPrice())
                    .sum();

            if (totalPrice != reservationRequestDto.getAmount()) {
                throw new AmountNotMatchException();
            }

            // 6. Create reservation
            Reservation reservation = Reservation.builder()
                    .userId(userId)
                    .showId(reservationRequestDto.getShowId())
                    .seatsReservedIds(seatIds)
                    .amountPaid(reservationRequestDto.getAmount())
                    .reservationStatus(ReservationStatus.PENDING_PAYMENT)
                    .build();

            reservationRepository.save(reservation);

            return reservation;
        } catch (Exception e) {
            seatServiceClient.unlockSeats(seatIds);
            throw e;
        }
    }


    @Override
    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getReservationStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Reservation is not pending payment");
        }

        try {
            seatServiceClient.updateSeatStatus(reservation.getSeatsReservedIds(), "BOOKED");

            reservation.setReservationStatus(ReservationStatus.CONFIRMED);
            reservation = reservationRepository.save(reservation);

            seatServiceClient.unlockSeats(reservation.getSeatsReservedIds());

            try {
                ShowDto show = showServiceClient.getShowById(reservation.getShowId());

                String payload = String.format(
                        "Đặt vé thành công! Mã đặt chỗ: #%d. Suất chiếu: %s lúc %s. Số ghế: %d. Tổng tiền: %.0f VND. " +
                                "Vui lòng đến rạp trước 15 phút.",
                        reservation.getId(),
                        show.getMovieId(),
                        show.getStartTime(),
                        reservation.getSeatsReservedIds().size(),
                        reservation.getAmountPaid()
                );

                NotificationRequestDto notificationDto = NotificationRequestDto.builder()
                        .userId(reservation.getUserId())
                        .type(NotificationType.RESERVATION)
                        .payload(payload)
                        .build();

                notificationServiceClient.addNotification(notificationDto);

            } catch (Exception e) {
                throw new RuntimeException("Failed to send confirmation notification for reservation {}");
                // Don't throw - notification failure shouldn't break the flow
            }
            return reservation;
        } catch (Exception e) {
            // Rollback: unlock seats và trả về PENDING_PAYMENT
            seatServiceClient.unlockSeats(reservation.getSeatsReservedIds());

            throw new RuntimeException("Failed to confirm reservation", e);
        }
    }

    @Override
    @Transactional
    public Reservation cancelReservationByPayment(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        try {
            seatServiceClient.updateSeatStatus(reservation.getSeatsReservedIds(), "UNBOOKED");
            seatServiceClient.unlockSeats(reservation.getSeatsReservedIds());

            reservation.setReservationStatus(ReservationStatus.CANCELED);
            reservation = reservationRepository.save(reservation);

            try {
                String payload = String.format(
                        "Thanh toán thất bại! Mã đặt chỗ #%d đã bị hủy. " +
                                "Ghế đã được giải phóng. Vui lòng thử lại.",
                        reservation.getId()
                );

                NotificationRequestDto notificationDto = NotificationRequestDto.builder()
                        .userId(reservation.getUserId())
                        .type(NotificationType.RESERVATION)
                        .payload(payload)
                        .build();

                notificationServiceClient.addNotification(notificationDto);

            } catch (Exception e) {
                System.out.println("Failed to send cancellation notification for reservation {}");
            }

            return reservation;

        } catch (Exception e) {
            seatServiceClient.unlockSeats(reservation.getSeatsReservedIds());
            throw new RuntimeException("Failed to cancel reservation", e);
        }
    }

    @Override
    public Reservation getReservationById(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

    }

    @Transactional
    @Override
    public Reservation cancelReservation(long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        // Check if show hasn't started
        ShowDto show = showServiceClient.getShowById(reservation.getShowId());
        if (show.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ShowStartedException(show.getId());
        }

        if (reservation.getReservationStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new RuntimeException("The reservation has been cancelled");
        }

        // Update seat status to UNBOOKED
        seatServiceClient.updateSeatStatus(reservation.getSeatsReservedIds(), "UNBOOKED");

        // Update reservation status
        reservation.setReservationStatus(ReservationStatus.CANCELED);

        String payload = String.format(
                "Huỷ vé thành công! Mã đặt chỗ: #%d. Số tiền hoàn: %.0f VND sẽ được hoàn lại trong 3-5 ngày làm việc.",
                reservation.getId(),
                reservation.getAmountPaid()
        );

        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                .userId(reservation.getUserId())
                .type(NotificationType.RESERVATION)
                .payload(payload)
                .build();

        notificationServiceClient.addNotification(notificationRequestDto);
        return reservationRepository.save(reservation);
    }

    @Override
    public PaginationResponse<Reservation> getAllReservationsForUser(Long userId, int page, int size) {
        UserDto user = userServiceClient.getUserById(userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Reservation> reservationPage = reservationRepository.findByUserId(userId, pageable);

        return PaginationResponse.<Reservation>builder()
                .pageNumber(reservationPage.getNumber())
                .pageSize(reservationPage.getSize())
                .totalPages(reservationPage.getTotalPages())
                .totalElements(reservationPage.getTotalElements())
                .data(reservationPage.getContent())
                .build();
    }
}
