package com.project.movie_reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private Long totalElements;
    private List<T> data;
}
