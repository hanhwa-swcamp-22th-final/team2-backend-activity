package com.team2.activity.command.service;

import com.team2.activity.command.repository.ActivityRepository;
import com.team2.activity.dto.ActivityUpdateRequest;
import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.entity.enums.Priority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityCommandService 테스트")
class ActivityCommandServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityCommandService activityCommandService;

    private Activity buildActivity() {
        return Activity.builder()
                .clientId(1L)
                .activityAuthorId(10L)
                .activityType(ActivityType.MEETING)
                .activityTitle("초기 미팅")
                .activityContent("초기 내용")
                .activityDate(LocalDate.of(2025, 4, 1))
                .build();
    }

    @Test
    @DisplayName("활동 생성 시 repository save 결과를 반환한다")
    void createActivity_returnsSavedActivity() {
        Activity activity = buildActivity();
        when(activityRepository.save(activity)).thenReturn(activity);

        Activity result = activityCommandService.createActivity(activity);

        assertThat(result).isSameAs(activity);
        verify(activityRepository).save(activity);
    }

    @Test
    @DisplayName("활동 수정 시 조회한 엔티티의 필드를 변경한다")
    void updateActivity_updatesLoadedEntity() {
        Activity activity = buildActivity();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        ActivityUpdateRequest request = new ActivityUpdateRequest(
                LocalDate.of(2025, 4, 5),
                ActivityType.ISSUE,
                "긴급 이슈",
                "우선 처리 필요",
                "PO-2025-001",
                Priority.HIGH,
                LocalDate.of(2025, 4, 6),
                LocalDate.of(2025, 4, 7)
        );
        Activity result = activityCommandService.updateActivity(1L, request, 99L);

        assertThat(result).isSameAs(activity);
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.ISSUE);
        assertThat(activity.getActivityTitle()).isEqualTo("긴급 이슈");
        assertThat(activity.getActivityContent()).isEqualTo("우선 처리 필요");
        assertThat(activity.getActivityDate()).isEqualTo(LocalDate.of(2025, 4, 5));
        assertThat(activity.getActivityAuthorId()).isEqualTo(99L);
        assertThat(activity.getPoId()).isEqualTo("PO-2025-001");
        assertThat(activity.getActivityPriority()).isEqualTo(Priority.HIGH);
        assertThat(activity.getActivityScheduleFrom()).isEqualTo(LocalDate.of(2025, 4, 6));
        assertThat(activity.getActivityScheduleTo()).isEqualTo(LocalDate.of(2025, 4, 7));
        verify(activityRepository).findById(1L);
    }

    @Test
    @DisplayName("수정 대상 활동이 없으면 예외를 던진다")
    void updateActivity_throwsWhenActivityDoesNotExist() {
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        ActivityUpdateRequest request = new ActivityUpdateRequest(
                LocalDate.of(2025, 4, 1), ActivityType.MEMO, "제목", "내용", null, null, null, null);
        assertThatThrownBy(() -> activityCommandService.updateActivity(999L, request, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("활동 삭제 시 조회한 엔티티를 삭제한다")
    void deleteActivity_deletesLoadedEntity() {
        Activity activity = buildActivity();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        activityCommandService.deleteActivity(1L);

        verify(activityRepository).findById(1L);
        verify(activityRepository).delete(activity);
    }
}
