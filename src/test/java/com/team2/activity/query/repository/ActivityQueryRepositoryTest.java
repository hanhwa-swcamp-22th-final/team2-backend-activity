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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Activity 읽기 전용 mapper가 SQL 결과를 엔티티로 정확히 매핑하는지 검증한다.
@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@Transactional
@DisplayName("ActivityQueryRepository 테스트")
class ActivityQueryRepositoryTest {

    // MyBatis 기반 조회를 수행할 실제 mapper 빈이다.
    @Autowired
    private ActivityQueryMapper activityQueryMapper;

    // 조회용 테스트 데이터를 직접 적재하기 위한 JDBC 도구다.
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 활동 레코드를 테스트 DB에 저장하고 조회 모델로 다시 읽어 온다.
    private Activity saveActivity(Long clientId, Long authorId, ActivityType type, String title, LocalDate date) {
        // activities 테이블에 조회용 활동 데이터를 직접 삽입한다.
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

        // 방금 저장된 활동의 PK를 조회한다.
        Long activityId = jdbcTemplate.queryForObject("SELECT MAX(activity_id) FROM activities", Long.class);
        // mapper 단건 조회 결과를 반환해 이후 검증에 재사용한다.
        return activityQueryMapper.findById(activityId);
    }

    @Test
    @DisplayName("ID로 활동 조회 시 읽기 모델 필드가 매핑된다")
    void findById_mapsActivityFields() {
        // 조회 대상 활동을 테스트 DB에 적재한다.
        Activity saved = saveActivity(1L, 10L, ActivityType.ISSUE, "긴급 이슈", LocalDate.of(2025, 4, 1));

        // mapper가 DB 레코드를 Activity 엔티티로 읽어 오도록 호출한다.
        Activity found = activityQueryMapper.findById(saved.getActivityId());

        // 기본 필드와 enum 필드가 모두 정확히 매핑되는지 확인한다.
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
        // 서로 다른 거래처의 활동 데이터를 함께 저장한다.
        saveActivity(1L, 10L, ActivityType.MEETING, "미팅1", LocalDate.of(2025, 4, 1));
        saveActivity(1L, 11L, ActivityType.MEMO, "메모1", LocalDate.of(2025, 4, 2));
        saveActivity(2L, 12L, ActivityType.ISSUE, "이슈1", LocalDate.of(2025, 4, 3));

        // client_id 조건으로 조회한 결과를 가져온다.
        List<Activity> result = activityQueryMapper.findByClientId(1L);

        // 요청한 거래처의 활동만 반환되는지 확인한다.
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getClientId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("활동 타입으로 활동 목록을 조회한다")
    void findByActivityType_returnsOnlyMatchedActivities() {
        // 여러 활동 타입을 섞어 저장한 뒤 ISSUE만 조회한다.
        saveActivity(1L, 10L, ActivityType.MEETING, "미팅1", LocalDate.of(2025, 4, 1));
        saveActivity(1L, 11L, ActivityType.ISSUE, "이슈1", LocalDate.of(2025, 4, 2));
        saveActivity(2L, 12L, ActivityType.ISSUE, "이슈2", LocalDate.of(2025, 4, 3));

        // activity_type 조건 조회 결과를 가져온다.
        List<Activity> result = activityQueryMapper.findByActivityType(ActivityType.ISSUE);

        // ISSUE 타입 활동만 반환되는지 확인한다.
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getActivityType)
                .containsOnly(ActivityType.ISSUE);
    }

    @Test
    @DisplayName("날짜 범위로 활동 목록을 조회한다")
    void findByDateRange_returnsOnlyMatchedActivities() {
        // 서로 다른 월의 활동 데이터를 저장한다.
        saveActivity(1L, 10L, ActivityType.MEETING, "3월 활동", LocalDate.of(2025, 3, 1));
        saveActivity(1L, 10L, ActivityType.MEETING, "4월 활동", LocalDate.of(2025, 4, 15));
        saveActivity(1L, 10L, ActivityType.MEETING, "5월 활동", LocalDate.of(2025, 5, 20));

        // 4월 범위로만 활동 목록을 조회한다.
        List<Activity> result = activityQueryMapper.findByDateRange(
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 30)
        );

        // 범위 안에 있는 활동만 반환되는지 확인한다.
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityTitle()).isEqualTo("4월 활동");
    }

    @Test
    @DisplayName("작성자 ID로 활동 목록을 조회한다")
    void findByAuthorId_returnsOnlyMatchedActivities() {
        // 서로 다른 작성자의 활동을 저장한다.
        saveActivity(1L, 10L, ActivityType.MEETING, "작성자10 활동1", LocalDate.of(2025, 4, 1));
        saveActivity(1L, 10L, ActivityType.MEMO, "작성자10 활동2", LocalDate.of(2025, 4, 2));
        saveActivity(1L, 20L, ActivityType.MEETING, "작성자20 활동", LocalDate.of(2025, 4, 3));

        // 작성자 ID 조건으로 결과를 조회한다.
        List<Activity> result = activityQueryMapper.findByAuthorId(10L);

        // 요청한 작성자의 활동만 반환되는지 확인한다.
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Activity::getActivityAuthorId)
                .containsOnly(10L);
    }

    @Test
    @DisplayName("거래처 ID와 활동 타입으로 활동 목록을 조회한다")
    void findByClientIdAndActivityType_returnsOnlyMatchedActivities() {
        // 복합 조건 검증을 위해 거래처와 활동 타입을 섞어 저장한다.
        saveActivity(1L, 10L, ActivityType.MEETING, "고객1 미팅", LocalDate.of(2025, 4, 1));
        saveActivity(1L, 10L, ActivityType.ISSUE, "고객1 이슈", LocalDate.of(2025, 4, 2));
        saveActivity(2L, 10L, ActivityType.MEETING, "고객2 미팅", LocalDate.of(2025, 4, 3));

        // client_id와 activity_type을 함께 조건으로 조회한다.
        List<Activity> result = activityQueryMapper.findByClientIdAndActivityType(1L, ActivityType.MEETING);

        // 두 조건을 모두 만족하는 활동만 반환되는지 확인한다.
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientId()).isEqualTo(1L);
        assertThat(result.get(0).getActivityType()).isEqualTo(ActivityType.MEETING);
    }
}
