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

// ActivityCommandService가 저장소를 통해 쓰기 로직을 수행하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityCommandService 테스트")
class ActivityCommandServiceTest {

    // 활동 저장/조회/삭제를 담당하는 repository 목 객체다.
    @Mock
    private ActivityRepository activityRepository;

    // repository를 호출하는 command 서비스 구현체다.
    @InjectMocks
    private ActivityCommandService activityCommandService;

    // 활동 command 테스트에 사용할 공통 엔티티를 생성한다.
    private Activity buildActivity() {
        return Activity.builder()
                // 테스트용 거래처 ID를 설정한다.
                .clientId(1L)
                // 테스트용 작성자 ID를 설정한다.
                .activityAuthorId(10L)
                // 테스트용 활동 타입을 설정한다.
                .activityType(ActivityType.MEETING)
                // 테스트용 활동 제목을 설정한다.
                .activityTitle("초기 미팅")
                // 테스트용 활동 본문을 설정한다.
                .activityContent("초기 내용")
                // 테스트용 활동 날짜를 설정한다.
                .activityDate(LocalDate.of(2025, 4, 1))
                // 공통 Activity 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("활동 생성 시 repository save 결과를 반환한다")
    void createActivity_returnsSavedActivity() {
        // 저장할 활동 엔티티와 save 결과를 준비한다.
        Activity activity = buildActivity();
        // repository save 호출 시 같은 엔티티를 반환하도록 설정한다.
        when(activityRepository.save(activity)).thenReturn(activity);

        // 서비스가 repository save 결과를 그대로 반환하는지 확인한다.
        Activity result = activityCommandService.createActivity(activity);

        // 반환 결과가 repository가 돌려준 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activity);
        // save가 정확히 한 번 호출됐는지 검증한다.
        verify(activityRepository).save(activity);
    }

    @Test
    @DisplayName("활동 수정 시 조회한 엔티티의 필드를 변경한다")
    void updateActivity_updatesLoadedEntity() {
        // 수정 대상 활동을 repository가 반환하도록 설정한다.
        Activity activity = buildActivity();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        // 서비스에 전달할 수정 요청을 준비한다.
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
        // 서비스 수정 호출 후 엔티티 필드가 변경됐는지 확인한다.
        Activity result = activityCommandService.updateActivity(1L, request, 99L);

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activity);
        // 활동 타입이 ISSUE로 바뀌었는지 확인한다.
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.ISSUE);
        // 활동 제목이 새 값으로 바뀌었는지 확인한다.
        assertThat(activity.getActivityTitle()).isEqualTo("긴급 이슈");
        // 활동 본문이 새 값으로 바뀌었는지 확인한다.
        assertThat(activity.getActivityContent()).isEqualTo("우선 처리 필요");
        // 활동 날짜가 새 값으로 바뀌었는지 확인한다.
        assertThat(activity.getActivityDate()).isEqualTo(LocalDate.of(2025, 4, 5));
        // 작성자 ID가 수정자 ID로 바뀌었는지 확인한다.
        assertThat(activity.getActivityAuthorId()).isEqualTo(99L);
        // PO ID가 새 값으로 바뀌었는지 확인한다.
        assertThat(activity.getPoId()).isEqualTo("PO-2025-001");
        // 우선순위가 새 값으로 바뀌었는지 확인한다.
        assertThat(activity.getActivityPriority()).isEqualTo(Priority.HIGH);
        // 일정 시작일이 새 값으로 바뀌었는지 확인한다.
        assertThat(activity.getActivityScheduleFrom()).isEqualTo(LocalDate.of(2025, 4, 6));
        // 일정 종료일이 새 값으로 바뀌었는지 확인한다.
        assertThat(activity.getActivityScheduleTo()).isEqualTo(LocalDate.of(2025, 4, 7));
        // 수정 전에 findById가 호출됐는지 검증한다.
        verify(activityRepository).findById(1L);
    }

    @Test
    @DisplayName("수정 대상 활동이 없으면 예외를 던진다")
    void updateActivity_throwsWhenActivityDoesNotExist() {
        // 조회 결과가 없으면 서비스가 예외를 던져야 한다.
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        // 예외 발생 검증용 수정 요청을 준비한다.
        ActivityUpdateRequest request = new ActivityUpdateRequest(
                LocalDate.of(2025, 4, 1), ActivityType.MEMO, "제목", "내용", null, null, null, null);
        // 없는 활동 수정 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityCommandService.updateActivity(999L, request, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("활동 삭제 시 조회한 엔티티를 삭제한다")
    void deleteActivity_deletesLoadedEntity() {
        // 삭제 대상 활동을 repository가 조회하도록 설정한다.
        Activity activity = buildActivity();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        // 서비스가 조회 후 delete를 호출하는지 확인한다.
        activityCommandService.deleteActivity(1L);

        // 삭제 전에 findById가 호출됐는지 검증한다.
        verify(activityRepository).findById(1L);
        // 조회된 엔티티가 delete 대상으로 전달됐는지 검증한다.
        verify(activityRepository).delete(activity);
    }
}
