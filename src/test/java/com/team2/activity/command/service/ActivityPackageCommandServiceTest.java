package com.team2.activity.command.service;

import com.team2.activity.command.repository.ActivityPackageRepository;
import com.team2.activity.dto.ActivityPackageUpdateRequest;
import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.entity.ActivityPackageItem;
import com.team2.activity.entity.ActivityPackageViewer;
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
    @DisplayName("패키지 기본 정보 수정 시 조회한 엔티티의 필드를 변경한다")
    void updatePackage_updatesLoadedEntity() {
        // 기본 정보 수정 대상 패키지를 조회하도록 설정한다.
        ActivityPackage activityPackage = buildPackage(List.of(1L, 2L), List.of(100L, 101L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        // 제목, 설명, PO 번호가 변경되는지 확인한다.
        ActivityPackage result = activityPackageCommandService.updatePackage(
                10L,
                "월간 패키지",
                "월간 활동 묶음",
                "PO-2025-002"
        );

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activityPackage);
        // 제목이 새 값으로 바뀌었는지 확인한다.
        assertThat(activityPackage.getPackageTitle()).isEqualTo("월간 패키지");
        // 설명이 새 값으로 바뀌었는지 확인한다.
        assertThat(activityPackage.getPackageDescription()).isEqualTo("월간 활동 묶음");
        // PO ID가 새 값으로 바뀌었는지 확인한다.
        assertThat(activityPackage.getPoId()).isEqualTo("PO-2025-002");
        // 수정 전에 findById가 호출됐는지 검증한다.
        verify(activityPackageRepository).findById(10L);
    }

    @Test
    @DisplayName("패키지 열람자 수정 시 기존 열람자를 교체한다")
    void updateViewers_replacesExistingViewers() {
        // 기존 열람자가 있는 패키지를 조회하도록 설정한다.
        ActivityPackage activityPackage = buildPackage(List.of(1L, 2L), List.of(100L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        // 열람자 목록이 새 값으로 완전히 교체되는지 확인한다.
        ActivityPackage result = activityPackageCommandService.updateViewers(10L, List.of(30L, 40L));

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activityPackage);
        // viewer 목록이 새 사용자 ID 목록으로 교체됐는지 확인한다.
        assertThat(activityPackage.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactly(30L, 40L);
        // 수정 전에 findById가 호출됐는지 검증한다.
        verify(activityPackageRepository).findById(10L);
    }

    @Test
    @DisplayName("패키지 활동 항목 수정 시 기존 항목을 교체한다")
    void updateItems_replacesExistingItems() {
        // 기존 활동 항목이 있는 패키지를 조회하도록 설정한다.
        ActivityPackage activityPackage = buildPackage(List.of(1L), List.of(100L, 101L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        // 활동 항목 목록이 새 값으로 교체되는지 확인한다.
        ActivityPackage result = activityPackageCommandService.updateItems(10L, List.of(200L, 300L));

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activityPackage);
        // item 목록이 새 활동 ID 목록으로 교체됐는지 확인한다.
        assertThat(activityPackage.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactly(200L, 300L);
        // 수정 전에 findById가 호출됐는지 검증한다.
        verify(activityPackageRepository).findById(10L);
    }

    @Test
    @DisplayName("수정 대상 패키지가 없으면 예외를 던진다")
    void updatePackage_throwsWhenPackageDoesNotExist() {
        // 조회 결과가 없으면 기본 정보 수정도 실패해야 한다.
        when(activityPackageRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 패키지 수정 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityPackageCommandService.updatePackage(
                999L,
                "제목",
                "설명",
                "PO-999"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("열람자 수정 시 대상 패키지가 없으면 예외를 던진다")
    void updateViewers_throwsWhenPackageDoesNotExist() {
        // 패키지가 없으면 열람자 수정도 예외여야 한다.
        when(activityPackageRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 패키지의 viewer 수정 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityPackageCommandService.updateViewers(999L, List.of(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("활동 항목 수정 시 대상 패키지가 없으면 예외를 던진다")
    void updateItems_throwsWhenPackageDoesNotExist() {
        // 패키지가 없으면 활동 항목 수정도 예외여야 한다.
        when(activityPackageRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 패키지의 item 수정 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityPackageCommandService.updateItems(999L, List.of(100L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
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
