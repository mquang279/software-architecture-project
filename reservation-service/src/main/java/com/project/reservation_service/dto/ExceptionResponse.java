package com.project.reservation_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class ExceptionResponse {
    private String title;
    private int status;
    private String detail;
    private String path;
    private Instant timestamp;
}
