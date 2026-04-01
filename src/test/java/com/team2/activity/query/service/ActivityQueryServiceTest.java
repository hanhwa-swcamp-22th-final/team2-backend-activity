package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
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

// ActivityQueryService가 조회 요청을 mapper에 올바르게 위임하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityQueryService 테스트")
class ActivityQueryServiceTest {

    // 읽기 전용 DB 조회를 담당하는 mapper 목 객체다.
    @Mock
    private ActivityQueryMapper activityQueryMapper;

    // mapper 결과를 그대로 감싸는 서비스 구현체다.
    @InjectMocks
    private ActivityQueryService activityQueryService;

    // 활동 조회 테스트에서 재사용할 공통 Activity 픽스처를 만든다.
    private Activity buildActivity(Long clientId, ActivityType activityType, String title) {
        return Activity.builder()
                // 테스트용 거래처 ID를 설정한다.
                .clientId(clientId)
                // 테스트용 작성자 ID를 설정한다.
                .activityAuthorId(10L)
                // 테스트용 활동 타입을 설정한다.
                .activityType(activityType)
                // 테스트용 활동 제목을 설정한다.
                .activityTitle(title)
                // 테스트용 활동 날짜를 설정한다.
                .activityDate(LocalDate.of(2025, 4, 1))
                // 공통 Activity 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("단건 조회 시 mapper 결과를 반환한다")
    void getActivity_returnsMappedActivity() {
        // mapper가 반환할 활동 엔티티를 준비한다.
        Activity activity = buildActivity(1L, ActivityType.MEETING, "거래처 미팅");
        // mapper findById 호출 시 같은 엔티티를 반환하도록 설정한다.
        when(activityQueryMapper.findById(1L)).thenReturn(activity);

        // 서비스가 mapper 결과를 그대로 반환하는지 확인한다.
        Activity result = activityQueryService.getActivity(1L);

        // 반환 결과가 mapper가 돌려준 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activity);
        // findById가 정확히 한 번 호출됐는지 검증한다.
        verify(activityQueryMapper).findById(1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 예외를 던진다")
    void getActivity_throwsWhenActivityDoesNotExist() {
        // mapper가 null을 반환하면 서비스가 예외로 변환해야 한다.
        when(activityQueryMapper.findById(999L)).thenReturn(null);

        // 없는 활동 조회 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityQueryService.getActivity(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 조회 시 mapper 목록을 그대로 반환한다")
    void getAllActivities_returnsMapperResult() {
        // mapper가 반환할 전체 활동 목록을 준비한다.
        List<Activity> activities = List.of(
                buildActivity(1L, ActivityType.MEETING, "미팅"),
                buildActivity(2L, ActivityType.ISSUE, "이슈")
        );
        // mapper findAll 호출 시 준비한 목록을 반환하도록 설정한다.
        when(activityQueryMapper.findAll()).thenReturn(activities);

        // 서비스가 목록을 가공하지 않고 그대로 반환하는지 확인한다.
        List<Activity> result = activityQueryService.getAllActivities();

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(activities);
        // findAll이 정확히 한 번 호출됐는지 검증한다.
        verify(activityQueryMapper).findAll();
    }

    @Test
    @DisplayName("거래처 ID 조건 조회를 위임한다")
    void getActivitiesByClientId_delegatesToMapper() {
        // 거래처 조건 조회 결과를 mapper가 반환하도록 설정한다.
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.MEMO, "메모"));
        // mapper가 거래처 조건 목록을 반환하도록 설정한다.
        when(activityQueryMapper.findByClientId(1L)).thenReturn(activities);

        // 서비스가 clientId 조건을 mapper로 그대로 전달하는지 확인한다.
        List<Activity> result = activityQueryService.getActivitiesByClientId(1L);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(activities);
        // findByClientId가 정확히 한 번 호출됐는지 검증한다.
        verify(activityQueryMapper).findByClientId(1L);
    }

    @Test
    @DisplayName("활동 타입 조건 조회를 위임한다")
    void getActivitiesByActivityType_delegatesToMapper() {
        // 활동 타입 필터 결과를 mapper가 반환하도록 설정한다.
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.ISSUE, "이슈"));
        // mapper가 활동 타입 조건 목록을 반환하도록 설정한다.
        when(activityQueryMapper.findByActivityType(ActivityType.ISSUE)).thenReturn(activities);

        // 서비스가 activityType 조건을 mapper에 위임하는지 확인한다.
        List<Activity> result = activityQueryService.getActivitiesByActivityType(ActivityType.ISSUE);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(activities);
        // findByActivityType이 정확히 한 번 호출됐는지 검증한다.
        verify(activityQueryMapper).findByActivityType(ActivityType.ISSUE);
    }

    @Test
    @DisplayName("날짜 범위 파라미터가 null이면 예외를 던진다")
    void getActivitiesByDateRange_throwsWhenParamIsNull() {
        // 날짜 범위 필수값 누락 시 mapper를 호출하기 전에 예외를 던져야 한다.
        // from 값이 없을 때 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityQueryService.getActivitiesByDateRange(null, LocalDate.of(2025, 4, 30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜 범위(from, to)는 필수입니다.");

        // to 값이 없을 때 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityQueryService.getActivitiesByDateRange(LocalDate.of(2025, 4, 1), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜 범위(from, to)는 필수입니다.");
    }

    @Test
    @DisplayName("날짜 범위 조건 조회를 위임한다")
    void getActivitiesByDateRange_delegatesToMapper() {
        // 날짜 범위 조건에 맞는 결과를 mapper가 반환하도록 설정한다.
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 4, 30);
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.SCHEDULE, "일정"));
        // mapper가 날짜 범위 조건 목록을 반환하도록 설정한다.
        when(activityQueryMapper.findByDateRange(from, to)).thenReturn(activities);

        // 서비스가 from/to를 그대로 mapper에 전달하는지 확인한다.
        List<Activity> result = activityQueryService.getActivitiesByDateRange(from, to);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(activities);
        // findByDateRange가 정확히 한 번 호출됐는지 검증한다.
        verify(activityQueryMapper).findByDateRange(from, to);
    }

    @Test
    @DisplayName("작성자 ID 조건 조회를 위임한다")
    void getActivitiesByAuthorId_delegatesToMapper() {
        // 작성자 기준 조회 결과를 mapper가 반환하도록 설정한다.
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.MEETING, "담당자 활동"));
        // mapper가 작성자 조건 목록을 반환하도록 설정한다.
        when(activityQueryMapper.findByAuthorId(10L)).thenReturn(activities);

        // 서비스가 작성자 조건 조회를 mapper에 위임하는지 확인한다.
        List<Activity> result = activityQueryService.getActivitiesByAuthorId(10L);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(activities);
        // findByAuthorId가 정확히 한 번 호출됐는지 검증한다.
        verify(activityQueryMapper).findByAuthorId(10L);
    }

    @Test
    @DisplayName("거래처 ID와 활동 타입 조건 조회를 위임한다")
    void getActivitiesByClientIdAndActivityType_delegatesToMapper() {
        // 복합 조건 조회 결과를 mapper가 반환하도록 설정한다.
        List<Activity> activities = List.of(buildActivity(1L, ActivityType.MEETING, "고객사 미팅"));
        // mapper가 복합 조건 목록을 반환하도록 설정한다.
        when(activityQueryMapper.findByClientIdAndActivityType(1L, ActivityType.MEETING)).thenReturn(activities);

        // 서비스가 clientId와 activityType을 함께 mapper에 전달하는지 확인한다.
        List<Activity> result = activityQueryService.getActivitiesByClientIdAndActivityType(1L, ActivityType.MEETING);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(activities);
        // findByClientIdAndActivityType이 정확히 한 번 호출됐는지 검증한다.
        verify(activityQueryMapper).findByClientIdAndActivityType(1L, ActivityType.MEETING);
    }
}
