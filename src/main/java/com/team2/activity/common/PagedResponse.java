package com.team2.activity.common;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage
) {
    public static <T> PagedResponse<T> of(List<T> items) {
        return new PagedResponse<>(items, items.size(), 1, 0);
    }
}
