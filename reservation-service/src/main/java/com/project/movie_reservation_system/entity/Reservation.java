package com.project.movie_reservation_system.entity;

import com.project.movie_reservation_system.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private Long userId;
    private Long showId;

    @ElementCollection
    @CollectionTable(name = "reservation_seats", joinColumns = @JoinColumn(name = "reservation_id"))
    @Column(name = "seat_reserved_id")
    private List<Long> seatsReservedIds;
    private double amountPaid;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus reservationStatus;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
