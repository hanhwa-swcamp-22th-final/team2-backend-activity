package com.team2.activity.query.repository;

import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;
import com.team2.activity.query.mapper.ActivityQueryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@Transactional
@DisplayName("Activity 동적 필터 쿼리 테스트")
class ActivityFilterQueryTest {

    @Autowired
    private ActivityQueryMapper activityQueryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        insertActivity(1L, 10L, ActivityType.MEETING, "글로벌 스틸 미팅", "PO-001", LocalDate.of(2026, 1, 15));
        insertActivity(1L, 10L, ActivityType.ISSUE, "글로벌 스틸 이슈", "PO-001", LocalDate.of(2026, 2, 10));
        insertActivity(2L, 20L, ActivityType.MEETING, "아시아 무역 미팅", "PO-002", LocalDate.of(2026, 1, 20));
        insertActivity(2L, 10L, ActivityType.MEMO, "아시아 무역 메모", null, LocalDate.of(2026, 3, 5));
        insertActivity(1L, 20L, ActivityType.SCHEDULE, "분기 일정", "PO-001", LocalDate.of(2026, 4, 1));
    }

    private void insertActivity(Long clientId, Long authorId, ActivityType type, String title, String poId, LocalDate date) {
        jdbcTemplate.update("""
                INSERT INTO activities (client_id, po_id, activity_author_id, activity_date, activity_type,
                    activity_title, activity_content, activity_priority, activity_schedule_from, activity_schedule_to, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                clientId, poId, authorId, date, type.getDisplayName(), title, title + " 내용",
                type == ActivityType.ISSUE ? Priority.HIGH.getDisplayName() : null, null, null,
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 9, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 9, 0)));
    }

    @Test
    @DisplayName("조건 없이 조회하면 전체 결과를 페이지 크기만큼 반환한다")
    void findWithFilters_noCondition_returnsAll() {
        List<Activity> result = activityQueryMapper.findWithFilters(null, null, null, null, null, null, null, 20, 0);
        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("clientId 단일 필터로 해당 거래처 활동만 반환한다")
    void findWithFilters_byClientId() {
        List<Activity> result = activityQueryMapper.findWithFilters(1L, null, null, null, null, null, null, 20, 0);
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(a -> a.getClientId().equals(1L));
    }

    @Test
    @DisplayName("activityType 필터로 해당 유형만 반환한다")
    void findWithFilters_byActivityType() {
        List<Activity> result = activityQueryMapper.findWithFilters(null, null, ActivityType.MEETING, null, null, null, null, 20, 0);
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getActivityType() == ActivityType.MEETING);
    }

    @Test
    @DisplayName("clientId + activityType 복합 필터가 정확히 동작한다")
    void findWithFilters_byClientIdAndType() {
        List<Activity> result = activityQueryMapper.findWithFilters(1L, null, ActivityType.MEETING, null, null, null, null, 20, 0);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityTitle()).isEqualTo("글로벌 스틸 미팅");
    }

    @Test
    @DisplayName("날짜 범위 필터가 정확히 동작한다")
    void findWithFilters_byDateRange() {
        List<Activity> result = activityQueryMapper.findWithFilters(null, null, null, null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 20, 0);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("keyword LIKE 검색이 제목에 대해 동작한다")
    void findWithFilters_byKeyword() {
        List<Activity> result = activityQueryMapper.findWithFilters(null, null, null, null, null, null, "스틸", 20, 0);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("clientId + authorId + dateRange 복합 필터가 동작한다")
    void findWithFilters_multipleConditions() {
        List<Activity> result = activityQueryMapper.findWithFilters(1L, null, null, 10L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, 20, 0);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("LIMIT/OFFSET 페이지네이션이 정확히 동작한다")
    void findWithFilters_pagination() {
        List<Activity> page0 = activityQueryMapper.findWithFilters(null, null, null, null, null, null, null, 2, 0);
        List<Activity> page1 = activityQueryMapper.findWithFilters(null, null, null, null, null, null, null, 2, 2);
        List<Activity> page2 = activityQueryMapper.findWithFilters(null, null, null, null, null, null, null, 2, 4);

        assertThat(page0).hasSize(2);
        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(1);
    }

    @Test
    @DisplayName("countWithFilters가 필터 조건에 맞는 총 개수를 반환한다")
    void countWithFilters_returnsCorrectCount() {
        long allCount = activityQueryMapper.countWithFilters(null, null, null, null, null, null, null);
        long client1Count = activityQueryMapper.countWithFilters(1L, null, null, null, null, null, null);
        long meetingCount = activityQueryMapper.countWithFilters(null, null, ActivityType.MEETING, null, null, null, null);

        assertThat(allCount).isEqualTo(5);
        assertThat(client1Count).isEqualTo(3);
        assertThat(meetingCount).isEqualTo(2);
    }

    @Test
    @DisplayName("매칭 결과 없으면 빈 리스트와 count 0을 반환한다")
    void findWithFilters_noMatch_returnsEmpty() {
        List<Activity> result = activityQueryMapper.findWithFilters(999L, null, null, null, null, null, null, 20, 0);
        long count = activityQueryMapper.countWithFilters(999L, null, null, null, null, null, null);

        assertThat(result).isEmpty();
        assertThat(count).isZero();
    }
}
