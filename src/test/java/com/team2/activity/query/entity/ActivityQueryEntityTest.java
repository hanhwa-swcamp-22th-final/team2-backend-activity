package com.team2.activity.query.entity;

import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.entity.enums.Priority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Query Activity 엔티티 테스트")
class ActivityQueryEntityTest {

    @Test
    @DisplayName("조회용 Activity는 기본 필드를 그대로 가진다")
    void createReadActivity_basicFields() {
        Activity activity = Activity.builder()
                .clientId(1L)
                .poId("PO-001")
                .activityAuthorId(10L)
                .activityDate(LocalDate.of(2025, 4, 1))
                .activityType(ActivityType.MEETING)
                .activityTitle("거래처 미팅")
                .activityContent("미팅 메모")
                .build();

        assertThat(activity.getClientId()).isEqualTo(1L);
        assertThat(activity.getPoId()).isEqualTo("PO-001");
        assertThat(activity.getActivityAuthorId()).isEqualTo(10L);
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.MEETING);
        assertThat(activity.getActivityTitle()).isEqualTo("거래처 미팅");
        assertThat(activity.getActivityContent()).isEqualTo("미팅 메모");
    }

    @Test
    @DisplayName("조회용 Activity는 이슈와 일정 확장 필드를 가진다")
    void createReadActivity_withIssueAndScheduleFields() {
        Activity activity = Activity.builder()
                .clientId(2L)
                .activityAuthorId(20L)
                .activityDate(LocalDate.of(2025, 5, 1))
                .activityType(ActivityType.ISSUE)
                .activityTitle("긴급 이슈")
                .activityPriority(Priority.HIGH)
                .activityScheduleFrom(LocalDate.of(2025, 5, 2))
                .activityScheduleTo(LocalDate.of(2025, 5, 3))
                .build();

        assertThat(activity.getActivityPriority()).isEqualTo(Priority.HIGH);
        assertThat(activity.getActivityScheduleFrom()).isEqualTo(LocalDate.of(2025, 5, 2));
        assertThat(activity.getActivityScheduleTo()).isEqualTo(LocalDate.of(2025, 5, 3));
    }
}
