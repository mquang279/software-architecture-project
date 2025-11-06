package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.PaymentRequestDto;
import com.project.movie_reservation_system.dto.PaymentResponseDto;
import com.project.movie_reservation_system.entity.Payment;
import com.project.movie_reservation_system.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDto> processPayment(
            @RequestBody PaymentRequestDto requestDto,
            @RequestParam Long userId) {

        PaymentResponseDto response = paymentService.processPayment(requestDto, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable Long paymentId) {
        PaymentResponseDto response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByReservation(@PathVariable Long reservationId) {
        PaymentResponseDto response = paymentService.getPaymentByReservationId(reservationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<PaginationResponse<Payment>> getMyPayments(@RequestParam Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PaginationResponse<Payment> payments = paymentService.getPaymentsByUserId(userId, page, size);
        return ResponseEntity.ok(payments);
    }
}