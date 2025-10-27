package com.project.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse<T> {
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private Long totalElements;
    private List<T> data;

    public PaginationResponse(Page<T> page) {
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.data = page.getContent();
    }
}
