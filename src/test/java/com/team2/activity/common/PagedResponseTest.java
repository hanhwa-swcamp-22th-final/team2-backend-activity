package com.team2.activity.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PagedResponse 페이지네이션 테스트")
class PagedResponseTest {

    private final List<String> items = List.of("A", "B", "C", "D", "E", "F", "G");

    @Test
    @DisplayName("of(items) — 단일 페이지 래핑")
    void of_singlePage() {
        PagedResponse<String> result = PagedResponse.of(items);

        assertThat(result.content()).hasSize(7);
        assertThat(result.totalElements()).isEqualTo(7);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.currentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("of(items, page, size) — 첫 페이지")
    void of_firstPage() {
        PagedResponse<String> result = PagedResponse.of(items, 0, 3);

        assertThat(result.content()).containsExactly("A", "B", "C");
        assertThat(result.totalElements()).isEqualTo(7);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.currentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("of(items, page, size) — 중간 페이지")
    void of_middlePage() {
        PagedResponse<String> result = PagedResponse.of(items, 1, 3);

        assertThat(result.content()).containsExactly("D", "E", "F");
        assertThat(result.currentPage()).isEqualTo(1);
    }

    @Test
    @DisplayName("of(items, page, size) — 마지막 페이지 (잔여 아이템)")
    void of_lastPage() {
        PagedResponse<String> result = PagedResponse.of(items, 2, 3);

        assertThat(result.content()).containsExactly("G");
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.currentPage()).isEqualTo(2);
    }

    @Test
    @DisplayName("of(items, page, size) — 범위 초과 페이지면 빈 리스트")
    void of_outOfRangePage() {
        PagedResponse<String> result = PagedResponse.of(items, 10, 3);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(7);
    }

    @Test
    @DisplayName("of(items, page, size) — 빈 리스트")
    void of_emptyList() {
        PagedResponse<String> result = PagedResponse.of(List.of(), 0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("of(content, totalElements, page, size) — DB count 기반 페이지네이션")
    void of_withDbCount() {
        List<String> pageContent = List.of("A", "B", "C");
        PagedResponse<String> result = PagedResponse.of(pageContent, 100, 0, 3);

        assertThat(result.content()).hasSize(3);
        assertThat(result.totalElements()).isEqualTo(100);
        assertThat(result.totalPages()).isEqualTo(34);
        assertThat(result.currentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("of(content, totalElements, page, size) — 두 번째 페이지")
    void of_withDbCount_secondPage() {
        List<String> pageContent = List.of("D", "E", "F");
        PagedResponse<String> result = PagedResponse.of(pageContent, 50, 1, 3);

        assertThat(result.content()).hasSize(3);
        assertThat(result.totalElements()).isEqualTo(50);
        assertThat(result.totalPages()).isEqualTo(17);
        assertThat(result.currentPage()).isEqualTo(1);
    }
}
