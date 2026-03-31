package com.team2.activity.command.service;

import com.team2.activity.command.repository.ActivityPackageRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityPackageCommandService 테스트")
class ActivityPackageCommandServiceTest {

    @Mock
    private ActivityPackageRepository activityPackageRepository;

    @InjectMocks
    private ActivityPackageCommandService activityPackageCommandService;

    private ActivityPackage buildPackage(List<Long> viewerIds, List<Long> activityIds) {
        return ActivityPackage.builder()
                .packageTitle("주간 패키지")
                .packageDescription("주간 활동 묶음")
                .poId("PO-001")
                .creatorId(7L)
                .viewers(viewerIds.stream().map(ActivityPackageViewer::of).toList())
                .items(activityIds.stream().map(ActivityPackageItem::of).toList())
                .build();
    }

    @Test
    @DisplayName("패키지 생성 시 repository save 결과를 반환한다")
    void createPackage_returnsSavedPackage() {
        ActivityPackage activityPackage = buildPackage(List.of(1L), List.of(100L));
        when(activityPackageRepository.save(activityPackage)).thenReturn(activityPackage);

        ActivityPackage result = activityPackageCommandService.createPackage(activityPackage);

        assertThat(result).isSameAs(activityPackage);
        verify(activityPackageRepository).save(activityPackage);
    }

    @Test
    @DisplayName("패키지 기본 정보 수정 시 조회한 엔티티의 필드를 변경한다")
    void updatePackage_updatesLoadedEntity() {
        ActivityPackage activityPackage = buildPackage(List.of(1L, 2L), List.of(100L, 101L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        ActivityPackage result = activityPackageCommandService.updatePackage(
                10L,
                "월간 패키지",
                "월간 활동 묶음",
                "PO-2025-002"
        );

        assertThat(result).isSameAs(activityPackage);
        assertThat(activityPackage.getPackageTitle()).isEqualTo("월간 패키지");
        assertThat(activityPackage.getPackageDescription()).isEqualTo("월간 활동 묶음");
        assertThat(activityPackage.getPoId()).isEqualTo("PO-2025-002");
        verify(activityPackageRepository).findById(10L);
    }

    @Test
    @DisplayName("패키지 열람자 수정 시 기존 열람자를 교체한다")
    void updateViewers_replacesExistingViewers() {
        ActivityPackage activityPackage = buildPackage(List.of(1L, 2L), List.of(100L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        ActivityPackage result = activityPackageCommandService.updateViewers(10L, List.of(30L, 40L));

        assertThat(result).isSameAs(activityPackage);
        assertThat(activityPackage.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactly(30L, 40L);
        verify(activityPackageRepository).findById(10L);
    }

    @Test
    @DisplayName("패키지 활동 항목 수정 시 기존 항목을 교체한다")
    void updateItems_replacesExistingItems() {
        ActivityPackage activityPackage = buildPackage(List.of(1L), List.of(100L, 101L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        ActivityPackage result = activityPackageCommandService.updateItems(10L, List.of(200L, 300L));

        assertThat(result).isSameAs(activityPackage);
        assertThat(activityPackage.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactly(200L, 300L);
        verify(activityPackageRepository).findById(10L);
    }

    @Test
    @DisplayName("수정 대상 패키지가 없으면 예외를 던진다")
    void updatePackage_throwsWhenPackageDoesNotExist() {
        when(activityPackageRepository.findById(999L)).thenReturn(Optional.empty());

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
        when(activityPackageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityPackageCommandService.updateViewers(999L, List.of(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("활동 항목 수정 시 대상 패키지가 없으면 예외를 던진다")
    void updateItems_throwsWhenPackageDoesNotExist() {
        when(activityPackageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityPackageCommandService.updateItems(999L, List.of(100L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("패키지 삭제 시 조회한 엔티티를 삭제한다")
    void deletePackage_deletesLoadedEntity() {
        ActivityPackage activityPackage = buildPackage(List.of(1L), List.of(100L));
        when(activityPackageRepository.findById(10L)).thenReturn(Optional.of(activityPackage));

        activityPackageCommandService.deletePackage(10L);

        verify(activityPackageRepository).findById(10L);
        verify(activityPackageRepository).delete(activityPackage);
    }
}
