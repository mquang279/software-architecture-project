package com.project.movie_reservation_system.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.movie_reservation_system.entity.Outbox;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

}
