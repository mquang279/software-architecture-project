package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.client.ReservationServiceClient;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.PaymentRequestDto;
import com.project.movie_reservation_system.dto.PaymentResponseDto;
import com.project.movie_reservation_system.entity.Payment;
import com.project.movie_reservation_system.enums.PaymentStatus;
import com.project.movie_reservation_system.repository.PaymentRepository;
import com.project.movie_reservation_system.service.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationServiceClient reservationServiceClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository, ReservationServiceClient reservationServiceClient) {
        this.paymentRepository = paymentRepository;
        this.reservationServiceClient = reservationServiceClient;
    }

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto, Long userId) {

        Payment payment = Payment.builder()
                .reservationId(requestDto.getReservationId())
                .userId(userId)
                .amount(requestDto.getAmount())
                .paymentMethod(requestDto.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        payment = paymentRepository.save(payment);

        //  Giả lập xử lý thanh toán (90% success rate)
        boolean paymentSuccess = simulatePayment();

        if (paymentSuccess) {
            // Payment thành công
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(Instant.now());
            paymentRepository.save(payment);

            try {
                reservationServiceClient.confirmReservation(payment.getReservationId());
            } catch (Exception e) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                return mapToResponseDto(payment, "Payment successful but reservation confirmation failed");
            }

            return mapToResponseDto(payment, "Payment processed successfully");

        } else {
            // Payment thất bại
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            // Thông báo cho Reservation Service hủy
            try {
                reservationServiceClient.cancelReservationByPayment(payment.getReservationId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to cancel reservation {}");
            }

            return mapToResponseDto(payment, "Payment failed. Please try again");
        }
    }

    @Override
    public PaymentResponseDto getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        return mapToResponseDto(payment, "Payment found");
    }

    @Override
    public PaymentResponseDto getPaymentByReservationId(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("Payment not found for reservation: " + reservationId));

        return mapToResponseDto(payment, "Payment found");
    }


    @Override
    public PaginationResponse<Payment> getPaymentsByUserId(Long userId, int page, int size) {
        Page<Payment> paymentPage = paymentRepository.findByUserId(userId, PageRequest.of(page,  size));
        List<Payment> payments = paymentPage.getContent();
        return PaginationResponse.<Payment>builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(paymentPage.getTotalPages())
                .totalElements(paymentPage.getTotalElements())
                .data(payments)
                .build();
    }


    /**
     * Giả lập xử lý thanh toán (90% thành công)
     */
    private boolean simulatePayment() {
        try {
            Thread.sleep(1000); // Giả lập delay xử lý
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Math.random() < 0.9; // 90% success rate
    }

    private PaymentResponseDto mapToResponseDto(Payment payment, String message) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .reservationId(payment.getReservationId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .message(message)
                .build();
    }
}
