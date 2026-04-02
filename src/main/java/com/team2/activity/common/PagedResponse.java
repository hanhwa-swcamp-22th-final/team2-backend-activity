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
    // 단순 리스트를 단일 페이지 응답으로 감싸서 반환한다.
    public static <T> PagedResponse<T> of(List<T> items) {
        return new PagedResponse<>(
                // 전달된 목록을 현재 페이지의 내용으로 사용한다.
                items,
                // 단일 페이지 응답이므로 전체 개수는 목록 크기와 같다.
                items.size(),
                // 빈 목록이면 0, 아니면 단일 페이지이므로 1이다.
                items.isEmpty() ? 0 : 1,
                // 첫 페이지를 의미하는 0을 현재 페이지로 사용한다.
                0
        );
    }
}
