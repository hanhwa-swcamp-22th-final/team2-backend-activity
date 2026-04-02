package com.team2.activity.command.application.service;

import com.team2.activity.command.domain.repository.ActivityPackageRepository;
import com.team2.activity.command.application.dto.ActivityPackageUpdateRequest;
import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.ActivityPackageViewer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ActivityPackageCommandService가 패키지 본문과 컬렉션 쓰기 로직을 처리하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityPackageCommandService 테스트")
class ActivityPackageCommandServiceTest {

    // 패키지 저장소 역할을 하는 repository 목 객체다.
    @Mock
    private ActivityPackageRepository activityPackageRepository;

    // repository를 이용해 패키지 변경을 처리하는 command 서비스다.
    @InjectMocks
    private ActivityPackageCommandService activityPackageCommandService;

    // 열람자와 활동 항목을 포함한 공통 패키지 픽스처를 만든다.
    private ActivityPackage buildPackage(List<Long> viewerIds, List<Long> activityIds) {
        return ActivityPackage.builder()
                // 테스트용 패키지 제목을 설정한다.
                .packageTitle("주간 패키지")
                // 테스트용 패키지 설명을 설정한다.
                .packageDescription("주간 활동 묶음")
                // 테스트용 PO ID를 설정한다.
                .poId("PO-001")
                // 테스트용 생성자 ID를 설정한다.
                .creatorId(7L)
                // viewer ID 목록을 viewer 엔티티 목록으로 변환해 설정한다.
                .viewers(viewerIds.stream().map(ActivityPackageViewer::of).toList())
                // activity ID 목록을 item 엔티티 목록으로 변환해 설정한다.
                .items(activityIds.stream().map(ActivityPackageItem::of).toList())
                // 공통 ActivityPackage 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("패키지 생성 시 repository save 결과를 반환한다")
    void createPackage_returnsSavedPackage() {
        // 저장할 패키지와 save 결과를 준비한다.
        ActivityPackage activityPackage = buildPackage(List.of(1L), List.of(100L));
        // repository save 호출 시 같은 패키지를 반환하도록 설정한다.
        when(activityPackageRepository.save(activityPackage)).thenReturn(activityPackage);

        // 서비스가 repository save 결과를 그대로 반환하는지 확인한다.
        ActivityPackage result = activityPackageCommandService.createPackage(activityPackage);

        // 반환 결과가 repository가 돌려준 패키지와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activityPackage);
        // save가 정확히 한 번 호출됐는지 검증한다.
        verify(activityPackageRepository).save(activityPackage);
    }

    @Test
    @DisplayName("패키지 전체 수정 시 단일 조회로 필드, 열람자, 항목을 교체한다")
    void updateAll_updatesPackageViewersAndItems() {
        // 전체 수정 대상 패키지를 조회하도록 설정한다.
        ActivityPackage activityPackage = buildPackage(List.of(1L, 2L), List.of(100L, 101L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        // 단일 요청으로 본문, 열람자, 항목이 모두 교체되는지 확인한다.
        ActivityPackageUpdateRequest request = new ActivityPackageUpdateRequest(
                "월간 패키지", "월간 활동 묶음", "PO-2025-002", List.of(200L, 300L), List.of(30L, 40L));
        ActivityPackage result = activityPackageCommandService.updateAll(10L, request);

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activityPackage);
        // 제목이 새 값으로 바뀌었는지 확인한다.
        assertThat(activityPackage.getPackageTitle()).isEqualTo("월간 패키지");
        // viewer 목록이 새 사용자 ID 목록으로 교체됐는지 확인한다.
        assertThat(activityPackage.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactly(30L, 40L);
        // item 목록이 새 활동 ID 목록으로 교체됐는지 확인한다.
        assertThat(activityPackage.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactly(200L, 300L);
        // 수정 전에 findById가 호출됐는지 검증한다.
        verify(activityPackageRepository).findById(10L);
    }

    @Test
    @DisplayName("패키지 전체 수정 시 viewerIds가 null이면 열람자를 변경하지 않는다")
    void updateAll_skipsViewersWhenNull() {
        // viewerIds null 요청 시 기존 viewer 목록이 유지되는지 확인한다.
        ActivityPackage activityPackage = buildPackage(List.of(1L, 2L), List.of(100L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        // viewerIds를 null로 전달해 viewer 교체가 건너뛰어지는지 확인한다.
        ActivityPackageUpdateRequest request = new ActivityPackageUpdateRequest(
                "월간 패키지", "월간 활동 묶음", "PO-002", List.of(200L), null);
        ActivityPackage result = activityPackageCommandService.updateAll(10L, request);

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activityPackage);
        // viewer 목록이 원래 값 그대로 유지됐는지 확인한다.
        assertThat(activityPackage.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactly(1L, 2L);
        // item 목록은 새 값으로 교체됐는지 확인한다.
        assertThat(activityPackage.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactly(200L);
    }

    @Test
    @DisplayName("패키지 전체 수정 시 activityIds가 null이면 빈 목록으로 처리한다")
    void updateAll_treatsNullActivityIdsAsEmpty() {
        // activityIds null 요청 시 item 목록이 비워지는지 확인한다.
        ActivityPackage activityPackage = buildPackage(List.of(1L), List.of(100L, 101L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        // activityIds를 null로 전달해 item 목록이 빈 목록으로 처리되는지 확인한다.
        ActivityPackageUpdateRequest request = new ActivityPackageUpdateRequest(
                "월간 패키지", "월간 활동 묶음", "PO-002", null, List.of(30L));
        ActivityPackage result = activityPackageCommandService.updateAll(10L, request);

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activityPackage);
        // item 목록이 비워졌는지 확인한다.
        assertThat(activityPackage.getItems()).isEmpty();
        // viewer 목록은 새 값으로 교체됐는지 확인한다.
        assertThat(activityPackage.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactly(30L);
    }

    @Test
    @DisplayName("패키지 전체 수정 시 대상 패키지가 없으면 예외를 던진다")
    void updateAll_throwsWhenPackageDoesNotExist() {
        // 조회 결과가 없으면 전체 수정도 실패해야 한다.
        when(activityPackageRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 패키지 전체 수정 시 IllegalArgumentException이 발생하는지 확인한다.
        ActivityPackageUpdateRequest request = new ActivityPackageUpdateRequest(
                "제목", "설명", "PO-999", List.of(1L), List.of(1L));
        assertThatThrownBy(() -> activityPackageCommandService.updateAll(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("패키지 삭제 시 대상 패키지가 없으면 예외를 던진다")
    void deletePackage_throwsWhenPackageDoesNotExist() {
        // 조회 결과가 없으면 삭제 요청도 예외여야 한다.
        when(activityPackageRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 패키지 삭제 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityPackageCommandService.deletePackage(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("패키지 삭제 시 조회한 엔티티를 삭제한다")
    void deletePackage_deletesLoadedEntity() {
        // 삭제 대상 패키지를 조회하도록 설정한다.
        ActivityPackage activityPackage = buildPackage(List.of(1L), List.of(100L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        // 서비스가 조회 후 delete를 호출하는지 확인한다.
        activityPackageCommandService.deletePackage(10L);

        // 삭제 전에 findById가 호출됐는지 검증한다.
        verify(activityPackageRepository).findById(10L);
        // 조회된 패키지가 delete 대상으로 전달됐는지 검증한다.
        verify(activityPackageRepository).delete(activityPackage);
    }
}
