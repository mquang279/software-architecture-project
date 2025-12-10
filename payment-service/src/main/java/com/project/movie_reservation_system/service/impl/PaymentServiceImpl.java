package com.project.movie_reservation_system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.movie_reservation_system.entity.Outbox;
import com.project.movie_reservation_system.entity.Payment;
import com.project.movie_reservation_system.enums.PaymentMethod;
import com.project.movie_reservation_system.enums.PaymentStatus;
import com.project.movie_reservation_system.event.model.PaymentFailEvent;
import com.project.movie_reservation_system.event.model.PaymentSuccessEvent;
import com.project.movie_reservation_system.repository.OutboxRepository;
import com.project.movie_reservation_system.repository.PaymentRepository;
import com.project.movie_reservation_system.service.PaymentService;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper mapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OutboxRepository outboxRepository,
            ObjectMapper mapper) {
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Override
    public void processPaymentForReservation(Long reservationId, Long userId, double totalPrice, List<Long> seatIds) {
        if (paymentRepository.findByReservationIdAndUserId(reservationId, userId).isPresent()) {
            throw new RuntimeException();
        }

        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .amount(totalPrice)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING).build();

        paymentRepository.save(payment);

        boolean isSuccess = simulatePayment();

        if (isSuccess) {
            handlePaymentSuccess(payment, seatIds);
        } else {
            handlePaymentFail(payment, seatIds);
        }
    }

    private void handlePaymentSuccess(Payment payment, List<Long> seatIds) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        PaymentSuccessEvent event = new PaymentSuccessEvent(payment.getReservationId(), payment.getUserId(),
                payment.getAmount(), seatIds);
        saveOutboxEvent(payment.getId(), "PAYMENT_SUCCESS", event);
    }

    private void handlePaymentFail(Payment payment, List<Long> seatIds) {
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        PaymentFailEvent event = new PaymentFailEvent(payment.getReservationId(), payment.getUserId(),
                payment.getAmount(), seatIds);
        saveOutboxEvent(payment.getId(), "PAYMENT_FAIL", event);
    }

    private void saveOutboxEvent(Long paymentId, String eventType, Object object) {
        try {
            String payload = mapper.writeValueAsString(object);
            Outbox event = Outbox.builder()
                    .aggregateType("payment")
                    .aggregateId(String.valueOf(paymentId))
                    .type(eventType)
                    .payload(payload)
                    .build();
            outboxRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize event payload", e);
        }
    }

    private boolean simulatePayment() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Math.random() < 0.5;
    }
}
