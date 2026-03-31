package com.team2.activity.query.service;

import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.query.mapper.ActivityQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityQueryService 테스트")
class ActivityQueryServiceTest {

    @Mock
    private ActivityQueryMapper activityQueryMapper;

    @InjectMocks
    private ActivityQueryService activityQueryService;

    private Activity buildActivity(Long clientId, ActivityType activityType, String title) {
        return Activity.builder()
                .clientId(clientId)
                .activityAuthorId(10L)
                .activityType(activityType)
                .activityTitle(title)
                .activityDate(LocalDate.of(2025, 4, 1))
                .build();
    }

    @Test
    @DisplayName("단건 조회 시 mapper 결과를 반환한다")
    void getActivity_returnsMappedActivity() {
        Activity activity = buildActivity(1L, ActivityType.MEETING, "거래처 미팅");
        when(activityQueryMapper.findById(1L)).thenReturn(activity);

        Activity result = activityQueryService.getActivity(1L);

        assertThat(result).isSameAs(activity);
        verify(activityQueryMapper).findById(1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 예외를 던진다")
    void getActivity_throwsWhenActivityDoesNotExist() {
        when(activityQueryMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> activityQueryService.getActivity(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 조회 시 mapper 목록을 그대로 반환한다")
    void getAllActivities_returnsMapperResult() {
        List<Activity> activities = List.of(
                buildActivity(1L, ActivityType.MEETING, "미팅"),
                buildActivity(2L, ActivityType.ISSUE, "이슈")
        );
        when(activityQueryMapper.findAll()).thenReturn(activities);

        List<Activity> result = activityQueryService.getAllActivities();

        assertThat(result).isEqualTo(activities);
        verify(activityQueryMapper).findAll();
    }

    @Test
    @DisplayName("거래처 ID 조건 조회를 위임한다")
    void getActivitiesByClientId_delegatesToMapper() {
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.MEMO, "메모"));
        when(activityQueryMapper.findByClientId(1L)).thenReturn(activities);

        List<Activity> result = activityQueryService.getActivitiesByClientId(1L);

        assertThat(result).isEqualTo(activities);
        verify(activityQueryMapper).findByClientId(1L);
    }

    @Test
    @DisplayName("활동 타입 조건 조회를 위임한다")
    void getActivitiesByActivityType_delegatesToMapper() {
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.ISSUE, "이슈"));
        when(activityQueryMapper.findByActivityType(ActivityType.ISSUE)).thenReturn(activities);

        List<Activity> result = activityQueryService.getActivitiesByActivityType(ActivityType.ISSUE);

        assertThat(result).isEqualTo(activities);
        verify(activityQueryMapper).findByActivityType(ActivityType.ISSUE);
    }

    @Test
    @DisplayName("날짜 범위 조건 조회를 위임한다")
    void getActivitiesByDateRange_delegatesToMapper() {
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 4, 30);
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.SCHEDULE, "일정"));
        when(activityQueryMapper.findByDateRange(from, to)).thenReturn(activities);

        List<Activity> result = activityQueryService.getActivitiesByDateRange(from, to);

        assertThat(result).isEqualTo(activities);
        verify(activityQueryMapper).findByDateRange(from, to);
    }

    @Test
    @DisplayName("작성자 ID 조건 조회를 위임한다")
    void getActivitiesByAuthorId_delegatesToMapper() {
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.MEETING, "담당자 활동"));
        when(activityQueryMapper.findByAuthorId(10L)).thenReturn(activities);

        List<Activity> result = activityQueryService.getActivitiesByAuthorId(10L);

        assertThat(result).isEqualTo(activities);
        verify(activityQueryMapper).findByAuthorId(10L);
    }

    @Test
    @DisplayName("거래처 ID와 활동 타입 조건 조회를 위임한다")
    void getActivitiesByClientIdAndActivityType_delegatesToMapper() {
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.MEETING, "고객사 미팅"));
        when(activityQueryMapper.findByClientIdAndActivityType(1L, ActivityType.MEETING)).thenReturn(activities);

        List<Activity> result = activityQueryService.getActivitiesByClientIdAndActivityType(1L, ActivityType.MEETING);

        assertThat(result).isEqualTo(activities);
        verify(activityQueryMapper).findByClientIdAndActivityType(1L, ActivityType.MEETING);
    }
}
