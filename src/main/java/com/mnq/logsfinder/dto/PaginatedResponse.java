package com.mnq.logsfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> items;
    private long totalItems;
    private int currentPage;
    private int itemsPerPage;
    private int totalPages;

    public PaginatedResponse(List<T> items, long totalItems, int currentPage, int itemsPerPage) {
        this.items = items;
        this.totalItems = totalItems;
        this.currentPage = currentPage;
        this.itemsPerPage = itemsPerPage;
        this.totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
    }
}

