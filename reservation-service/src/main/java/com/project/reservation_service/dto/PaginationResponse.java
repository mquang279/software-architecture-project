package com.project.reservation_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginationResponse<T> {
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private Long totalElements;
    private List<T> data;
}
