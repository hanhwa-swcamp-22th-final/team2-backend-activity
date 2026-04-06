package com.team2.activity.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// 목록 API에서 공통으로 사용하는 단순 페이징 응답 래퍼다.
@Schema(description = "페이징 응답 래퍼")
public record PagedResponse<T>(
        @Schema(description = "현재 페이지 데이터 목록") List<T> content,
        @Schema(description = "전체 데이터 개수") long totalElements,
        @Schema(description = "전체 페이지 수") int totalPages,
        @Schema(description = "현재 페이지 번호") int currentPage
) {
    public static <T> PagedResponse<T> of(List<T> items) {
        return new PagedResponse<>(items, items.size(), items.isEmpty() ? 0 : 1, 0);
    }

    public static <T> PagedResponse<T> of(List<T> allItems, int page, int size) {
        int total = allItems.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<T> pageContent = allItems.subList(fromIndex, toIndex);
        return new PagedResponse<>(pageContent, total, totalPages, page);
    }

    public static <T> PagedResponse<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PagedResponse<>(content, totalElements, totalPages, page);
    }
}
