package com.team2.activity.common;

import java.util.List;

// 목록 API에서 공통으로 사용하는 단순 페이징 응답 래퍼다.
public record PagedResponse<T>(
        // 현재 페이지의 데이터 목록이다.
        List<T> content,
        // 전체 데이터 개수다.
        long totalElements,
        // 전체 페이지 수다.
        int totalPages,
        // 현재 페이지 번호다.
        int currentPage
) {
    public static <T> PagedResponse<T> of(List<T> items) {
        return new PagedResponse<>(items, items.size(), items.isEmpty() ? 0 : 1, 0);
    }

    public static <T> PagedResponse<T> of(List<T> allItems, int page, int size) {
        int totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<T> pageContent = allItems.subList(fromIndex, toIndex);
        return new PagedResponse<>(pageContent, totalElements, totalPages, page);
    }
}
