# Microservices Movie Reservation System

## Overview

This project is a robust refactoring and enhancement of a monolithic Movie Reservation System.

The primary goal of this project was to migrate the legacy architecture into a scalable, fault-tolerant Microservices Architecture. It demonstrates advanced distributed systems patterns including Saga, Outbox, Circuit Breaker, and Rate Limiting to handle high concurrency and ensure data consistency across distributed services.

## Key Features & Enhancements

### 1. Microservices Migration
Decomposed the monolithic application into loosely coupled, domain-specific services. Each service owns its database (Database-per-service pattern) to ensure loose coupling.

### 2. Distributed Patterns Implemented

*   **Circuit Breaker**: Implemented to prevent cascading failures. If a downstream service (e.g., Payment) is unresponsive, the system fails fast and provides a fallback mechanism rather than hanging the entire request.
*   **Rate Limiter**: Implemented to protect services from being overwhelmed by high traffic spikes. This ensures stability during high-demand periods.
*   **Cache-Aside Pattern**: Integrated Redis to cache frequently accessed data (such as Movie and Show details). This significantly reduces database load and improves read latency for end-users.
*   **Saga Pattern**: Managed distributed transactions for the booking workflow.
    *   The reservation process spans multiple services (Reservation -> Payment -> Seat).
    *   If any step fails, compensating events are published to roll back changes (e.g., refunding payments or unlocking seats), ensuring eventual consistency.
*   **Outbox Pattern**: Solved the "Dual Write Problem" to ensure reliable messaging. Instead of writing to the database and publishing to the message broker simultaneously, services write to an internal outbox table. A CDC (Change Data Capture) process then reliably pushes these events to Kafka.
*   **Gateway Routing**: Centralized entry point using an API Gateway. This handles request routing, load balancing, and cross-cutting concerns like authentication.

## System Architecture

### Services

| Service | Description |
| :--- | :--- |
| **Gateway Service** | API Gateway, Routing, Rate Limiting |
| **Discovery Service** | Service Registry (Eureka) |
| **Auth Service** | JWT Authentication & Authorization |
| **User Service** | User profile and management |
| **Movie Service** | Manages movie metadata (Cached with Redis) |
| **Show Service** | Manages showtimes and theater associations |
| **Reservation Service** | Core booking logic, initiates Saga workflow |
| **Payment Service** | Payment processing |
| **Seat Service** | Seat inventory and locking mechanism |
| **Notification Service** | Sends email/SMS notifications (Async) |

### Tech Stack

*   **Languages**: Java (Spring Boot)
*   **Databases**: PostgreSQL / MySQL
*   **Messaging**: Apache Kafka, Zookeeper
*   **Caching**: Redis
*   **Service Discovery**: Netflix Eureka
*   **API Gateway**: Spring Cloud Gateway
*   **Integration**: Kafka Connect (Debezium)
*   **Containerization**: Docker, Docker Compose

## Getting Started

### Prerequisites

*   Docker & Docker Compose installed on your machine.
*   Java/Maven (optional, if running locally without Docker).

### Installation & Running

1.  **Clone the repository**
    ```bash
    git clone https://github.com/mquang279/software-architecture-project.git
    cd software-architecture-project
    ```

2.  **Build the services**
    Build the project artifacts:
    ```bash
    ./mvnw clean package -DskipTests
    ```

3.  **Start the Infrastructure**
    Run the entire system using Docker Compose:
    ```bash
    docker-compose up -d
    ```
    This will start the databases, Kafka, Redis, Eureka, and all microservices.

4.  **Configure Outbox Connectors**
    If the project uses Debezium/Kafka Connect for the Outbox pattern, register the connectors:
    ```bash
    # Run the provided script to register connectors (if applicable)
    ./kafka-connect.sh
    ```

### Verifying the Deployment

*   **Eureka Dashboard**: Access `http://localhost:8761` to view registered services.
*   **API Gateway**: Access the system via `http://localhost:8080`.

## Credits

*   **Original Project**: [jagmeetsingh1309/movie-reservation-system](https://github.com/jagmeetsingh1309/movie-reservation-system) - The monolithic base this project was migrated from.

