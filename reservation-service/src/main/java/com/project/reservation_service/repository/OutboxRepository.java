package com.project.reservation_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.reservation_service.entity.Outbox;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

}
