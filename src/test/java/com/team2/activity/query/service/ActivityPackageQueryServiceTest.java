package com.team2.activity.query.service;

import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.query.mapper.ActivityPackageQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityPackageQueryService 테스트")
class ActivityPackageQueryServiceTest {

    @Mock
    private ActivityPackageQueryMapper activityPackageQueryMapper;

    @InjectMocks
    private ActivityPackageQueryService activityPackageQueryService;

    private ActivityPackage buildPackage(String title, Long creatorId) {
        return ActivityPackage.builder()
                .packageTitle(title)
                .packageDescription("패키지 설명")
                .poId("PO-001")
                .creatorId(creatorId)
                .build();
    }

    @Test
    @DisplayName("단건 조회 시 mapper 결과를 반환한다")
    void getPackage_returnsMappedPackage() {
        ActivityPackage activityPackage = buildPackage("주간 패키지", 7L);
        when(activityPackageQueryMapper.findById(1L)).thenReturn(activityPackage);

        ActivityPackage result = activityPackageQueryService.getPackage(1L);

        assertThat(result).isSameAs(activityPackage);
        verify(activityPackageQueryMapper).findById(1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 예외를 던진다")
    void getPackage_throwsWhenPackageDoesNotExist() {
        when(activityPackageQueryMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> activityPackageQueryService.getPackage(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 조회 시 mapper 목록을 그대로 반환한다")
    void getAllPackages_returnsMapperResult() {
        List<ActivityPackage> packages = List.of(
                buildPackage("주간 패키지", 7L),
                buildPackage("월간 패키지", 8L)
        );
        when(activityPackageQueryMapper.findAll()).thenReturn(packages);

        List<ActivityPackage> result = activityPackageQueryService.getAllPackages();

        assertThat(result).isEqualTo(packages);
        verify(activityPackageQueryMapper).findAll();
    }

    @Test
    @DisplayName("생성자 ID 조건 조회를 위임한다")
    void getPackagesByCreatorId_delegatesToMapper() {
        List<ActivityPackage> packages = List.of(buildPackage("주간 패키지", 7L));
        when(activityPackageQueryMapper.findAllByCreatorId(7L)).thenReturn(packages);

        List<ActivityPackage> result = activityPackageQueryService.getPackagesByCreatorId(7L);

        assertThat(result).isEqualTo(packages);
        verify(activityPackageQueryMapper).findAllByCreatorId(7L);
    }
}
