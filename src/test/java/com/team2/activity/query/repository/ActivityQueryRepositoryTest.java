package com.team2.activity.query.repository;

import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.entity.enums.Priority;
import com.team2.activity.query.mapper.ActivityQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@DisplayName("ActivityQueryRepository 테스트")
class ActivityQueryRepositoryTest {

    @Autowired
    private ActivityQueryMapper activityQueryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Activity saveActivity(Long clientId, Long authorId, ActivityType type, String title, LocalDate date) {
        jdbcTemplate.update(
                """
                INSERT INTO activities (
                    client_id, po_id, activity_author_id, activity_date, activity_type,
                    activity_title, activity_content, activity_priority,
                    activity_schedule_from, activity_schedule_to, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                clientId,
                "PO-" + clientId,
                authorId,
                date,
                type.getDisplayName(),
                title,
                title + " 내용",
                type == ActivityType.ISSUE ? Priority.HIGH.getDisplayName() : null,
                null,
                null,
                Timestamp.valueOf(LocalDateTime.of(2025, 4, 1, 9, 0)),
                Timestamp.valueOf(LocalDateTime.of(2025, 4, 1, 9, 0))
        );

        Long activityId = jdbcTemplate.queryForObject("SELECT MAX(activity_id) FROM activities", Long.class);
        return activityQueryMapper.findById(activityId);
    }

    @Test
    @DisplayName("ID로 활동 조회 시 읽기 모델 필드가 매핑된다")
    void findById_mapsActivityFields() {
        Activity saved = saveActivity(1L, 10L, ActivityType.ISSUE, "긴급 이슈", LocalDate.of(2025, 4, 1));

        Activity found = activityQueryMapper.findById(saved.getActivityId());

        assertThat(found).isNotNull();
        assertThat(found.getActivityId()).isEqualTo(saved.getActivityId());
        assertThat(found.getClientId()).isEqualTo(1L);
        assertThat(found.getActivityAuthorId()).isEqualTo(10L);
        assertThat(found.getActivityType()).isEqualTo(ActivityType.ISSUE);
        assertThat(found.getActivityPriority()).isEqualTo(Priority.HIGH);
        assertThat(found.getActivityTitle()).isEqualTo("긴급 이슈");
    }

    @Test
    @DisplayName("거래처 ID로 활동 목록을 조회한다")
    void findByClientId_returnsOnlyMatchedActivities() {
        saveActivity(1L, 10L, ActivityType.MEETING, "미팅1", LocalDate.of(2025, 4, 1));
        saveActivity(1L, 11L, ActivityType.MEMO, "메모1", LocalDate.of(2025, 4, 2));
        saveActivity(2L, 12L, ActivityType.ISSUE, "이슈1", LocalDate.of(2025, 4, 3));

        List<Activity> result = activityQueryMapper.findByClientId(1L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getClientId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("활동 타입으로 활동 목록을 조회한다")
    void findByActivityType_returnsOnlyMatchedActivities() {
        saveActivity(1L, 10L, ActivityType.MEETING, "미팅1", LocalDate.of(2025, 4, 1));
        saveActivity(1L, 11L, ActivityType.ISSUE, "이슈1", LocalDate.of(2025, 4, 2));
        saveActivity(2L, 12L, ActivityType.ISSUE, "이슈2", LocalDate.of(2025, 4, 3));

        List<Activity> result = activityQueryMapper.findByActivityType(ActivityType.ISSUE);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getActivityType)
                .containsOnly(ActivityType.ISSUE);
    }

    @Test
    @DisplayName("날짜 범위로 활동 목록을 조회한다")
    void findByDateRange_returnsOnlyMatchedActivities() {
        saveActivity(1L, 10L, ActivityType.MEETING, "3월 활동", LocalDate.of(2025, 3, 1));
        saveActivity(1L, 10L, ActivityType.MEETING, "4월 활동", LocalDate.of(2025, 4, 15));
        saveActivity(1L, 10L, ActivityType.MEETING, "5월 활동", LocalDate.of(2025, 5, 20));

        List<Activity> result = activityQueryMapper.findByDateRange(
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 30)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityTitle()).isEqualTo("4월 활동");
    }

    @Test
    @DisplayName("작성자 ID로 활동 목록을 조회한다")
    void findByAuthorId_returnsOnlyMatchedActivities() {
        saveActivity(1L, 10L, ActivityType.MEETING, "작성자10 활동1", LocalDate.of(2025, 4, 1));
        saveActivity(1L, 10L, ActivityType.MEMO, "작성자10 활동2", LocalDate.of(2025, 4, 2));
        saveActivity(1L, 20L, ActivityType.MEETING, "작성자20 활동", LocalDate.of(2025, 4, 3));

        List<Activity> result = activityQueryMapper.findByAuthorId(10L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getActivityAuthorId)
                .containsOnly(10L);
    }

    @Test
    @DisplayName("거래처 ID와 활동 타입으로 활동 목록을 조회한다")
    void findByClientIdAndActivityType_returnsOnlyMatchedActivities() {
        saveActivity(1L, 10L, ActivityType.MEETING, "고객1 미팅", LocalDate.of(2025, 4, 1));
        saveActivity(1L, 10L, ActivityType.ISSUE, "고객1 이슈", LocalDate.of(2025, 4, 2));
        saveActivity(2L, 10L, ActivityType.MEETING, "고객2 미팅", LocalDate.of(2025, 4, 3));

        List<Activity> result = activityQueryMapper.findByClientIdAndActivityType(1L, ActivityType.MEETING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientId()).isEqualTo(1L);
        assertThat(result.get(0).getActivityType()).isEqualTo(ActivityType.MEETING);
    }
}
