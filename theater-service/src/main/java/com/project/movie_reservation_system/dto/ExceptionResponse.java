package com.project.movie_reservation_system.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExceptionResponse {
    private String title;
    private int status;
    private String detail;
    private String path;
    private Instant timestamp;
}
