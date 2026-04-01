package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
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

// ActivityPackageQueryService가 조회 요청을 mapper에 정확히 위임하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityPackageQueryService 테스트")
class ActivityPackageQueryServiceTest {

    // 패키지 읽기 모델을 조회하는 mapper 목 객체다.
    @Mock
    private ActivityPackageQueryMapper activityPackageQueryMapper;

    // mapper를 감싼 조회 서비스 구현체다.
    @InjectMocks
    private ActivityPackageQueryService activityPackageQueryService;

    // 패키지 조회 테스트에 사용할 공통 픽스처를 생성한다.
    private ActivityPackage buildPackage(String title, Long creatorId) {
        return ActivityPackage.builder()
                // 테스트용 패키지 제목을 설정한다.
                .packageTitle(title)
                // 테스트용 패키지 설명을 설정한다.
                .packageDescription("패키지 설명")
                // 테스트용 PO ID를 설정한다.
                .poId("PO-001")
                // 테스트용 생성자 ID를 설정한다.
                .creatorId(creatorId)
                // 공통 ActivityPackage 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("단건 조회 시 mapper 결과를 반환한다")
    void getPackage_returnsMappedPackage() {
        // mapper가 반환할 패키지 엔티티를 준비한다.
        ActivityPackage activityPackage = buildPackage("주간 패키지", 7L);
        // mapper findById 호출 시 같은 엔티티를 반환하도록 설정한다.
        when(activityPackageQueryMapper.findById(1L)).thenReturn(activityPackage);

        // 서비스가 mapper 결과를 그대로 반환하는지 확인한다.
        ActivityPackage result = activityPackageQueryService.getPackage(1L);

        // 반환 결과가 mapper가 돌려준 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(activityPackage);
        // findById가 정확히 한 번 호출됐는지 검증한다.
        verify(activityPackageQueryMapper).findById(1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 예외를 던진다")
    void getPackage_throwsWhenPackageDoesNotExist() {
        // 조회 결과가 없으면 서비스가 예외로 변환해야 한다.
        when(activityPackageQueryMapper.findById(999L)).thenReturn(null);

        // 없는 패키지 조회 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> activityPackageQueryService.getPackage(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 패키지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 조회 시 mapper 목록을 그대로 반환한다")
    void getAllPackages_returnsMapperResult() {
        // mapper가 반환할 패키지 목록을 준비한다.
        List<ActivityPackage> packages = List.of(
                buildPackage("주간 패키지", 7L),
                buildPackage("월간 패키지", 8L)
        );
        // mapper findAll 호출 시 준비한 목록을 반환하도록 설정한다.
        when(activityPackageQueryMapper.findAll()).thenReturn(packages);

        // 전체 조회 결과가 그대로 전달되는지 확인한다.
        List<ActivityPackage> result = activityPackageQueryService.getAllPackages();

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(packages);
        // findAll이 정확히 한 번 호출됐는지 검증한다.
        verify(activityPackageQueryMapper).findAll();
    }

    @Test
    @DisplayName("생성자 ID 조건 조회를 위임한다")
    void getPackagesByCreatorId_delegatesToMapper() {
        // creatorId 조건 조회 결과를 mapper가 반환하도록 설정한다.
        List<ActivityPackage> packages = List.of(buildPackage("주간 패키지", 7L));
        // mapper가 creatorId 조건 목록을 반환하도록 설정한다.
        when(activityPackageQueryMapper.findAllByCreatorId(7L)).thenReturn(packages);

        // 서비스가 생성자 조건 조회를 mapper에 위임하는지 확인한다.
        List<ActivityPackage> result = activityPackageQueryService.getPackagesByCreatorId(7L);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(packages);
        // findAllByCreatorId가 정확히 한 번 호출됐는지 검증한다.
        verify(activityPackageQueryMapper).findAllByCreatorId(7L);
    }
}
