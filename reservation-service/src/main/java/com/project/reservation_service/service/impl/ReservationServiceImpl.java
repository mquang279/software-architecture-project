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
        if (user == null) {
            throw new CustomException(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        // 2. Validate show exists
        ShowDto show = showServiceClient.getShowById(reservationRequestDto.getShowId());
        if (show == null) {
            throw new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        // 3. Lock seats
        List<Long> seatIds = reservationRequestDto.getSeatIdsToReserve();
        try {
            seatServiceClient.lockSeats(seatIds);

            // 4. Validate amount
            double totalPrice = seatIds.stream()
                    .mapToDouble(id -> seatServiceClient.getSeatById(id).getPrice())
                    .sum();

            if (totalPrice != reservationRequestDto.getAmount()) {
                throw new AmountNotMatchException(AMOUNT_NOT_MATCH, HttpStatus.BAD_REQUEST);
            }

            // 5. Mark seats as BOOKED
            seatServiceClient.updateSeatStatus(seatIds, "BOOKED");

            // 6. Create reservation
            Reservation reservation = Reservation.builder()
                    .userId(userId)
                    .showId(reservationRequestDto.getShowId())
                    .seatsReservedIds(seatIds)
                    .amountPaid(reservationRequestDto.getAmount())
                    .reservationStatus(ReservationStatus.BOOKED)
                    .build();

            reservationRepository.save(reservation);


            String payload = String.format(
                    "Đặt vé thành công! Mã đặt chỗ: #%d.Chiếu: %s lúc %s. Số ghế: %d. Tổng tiền: %.0f VND.",
                    reservation.getId(),
                    show.getMovieId(),
                    show.getStartTime(),
                    reservation.getSeatsReservedIds().size(),
                    reservation.getAmountPaid()
            );


            NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                    .userId(userId)
                    .type(NotificationType.RESERVATION)
                    .payload(payload)
                    .build();

            notificationServiceClient.addNotification(notificationRequestDto);
            return reservation;
        } finally {
            seatServiceClient.unlockSeats(seatIds);
        }

    }

    @Override
    public Reservation getReservationById(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(RESERVATION_NOT_FOUND, HttpStatus.NOT_FOUND));

    }

    @Transactional
    @Override
    public Reservation cancelReservation(long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(RESERVATION_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check if show hasn't started
        ShowDto show = showServiceClient.getShowById(reservation.getShowId());
        if (show.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ShowStartedException(SHOW_STARTED_EXCEPTION, HttpStatus.BAD_REQUEST);
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

        if (user == null) {
            throw new CustomException(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

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
