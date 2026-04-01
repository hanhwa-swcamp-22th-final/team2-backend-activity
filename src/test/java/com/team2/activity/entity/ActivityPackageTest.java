package com.team2.activity.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ActivityPackage 엔티티 테스트")
class ActivityPackageTest {

    @Autowired
    private TestEntityManager em;

    private ActivityPackage buildBasicPackage() {
        return ActivityPackage.builder()
                .packageTitle("2025 Q1 영업 활동 패키지")
                .packageDescription("1분기 주요 거래처 활동 모음")
                .poId("PO-2025-001")
                .creatorId(1L)
                .viewers(List.of(ActivityPackageViewer.of(2L), ActivityPackageViewer.of(3L)))
                .items(List.of(ActivityPackageItem.of(100L), ActivityPackageItem.of(101L), ActivityPackageItem.of(102L)))
                .build();
    }

    @Test
    @DisplayName("기본 ActivityPackage 생성 성공")
    void createActivityPackage_basic() {
        ActivityPackage pkg = buildBasicPackage();

        assertThat(pkg.getPackageTitle()).isEqualTo("2025 Q1 영업 활동 패키지");
        assertThat(pkg.getPackageDescription()).isEqualTo("1분기 주요 거래처 활동 모음");
        assertThat(pkg.getPoId()).isEqualTo("PO-2025-001");
        assertThat(pkg.getCreatorId()).isEqualTo(1L);
        assertThat(pkg.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactly(2L, 3L);
        assertThat(pkg.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactly(100L, 101L, 102L);
    }

    @Test
    @DisplayName("선택 필드 없이 ActivityPackage 생성")
    void createActivityPackage_withoutOptionalFields() {
        ActivityPackage pkg = ActivityPackage.builder()
                .packageTitle("기본 패키지")
                .creatorId(1L)
                .build();

        assertThat(pkg.getPackageDescription()).isNull();
        assertThat(pkg.getPoId()).isNull();
    }

    @Test
    @DisplayName("viewers null 전달 시 빈 리스트로 초기화")
    void createActivityPackage_nullViewers() {
        ActivityPackage pkg = ActivityPackage.builder()
                .packageTitle("패키지")
                .creatorId(1L)
                .viewers(null)
                .items(null)
                .build();

        assertThat(pkg.getViewers()).isNotNull().isEmpty();
        assertThat(pkg.getItems()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("복수 열람 권한 및 포함 활동기록 저장")
    void createActivityPackage_multipleViewersAndItems() {
        ActivityPackage pkg = ActivityPackage.builder()
                .packageTitle("대용량 패키지")
                .creatorId(1L)
                .viewers(List.of(
                        ActivityPackageViewer.of(2L),
                        ActivityPackageViewer.of(3L),
                        ActivityPackageViewer.of(4L),
                        ActivityPackageViewer.of(5L)))
                .items(List.of(
                        ActivityPackageItem.of(10L),
                        ActivityPackageItem.of(20L),
                        ActivityPackageItem.of(30L)))
                .build();

        assertThat(pkg.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactlyInAnyOrder(2L, 3L, 4L, 5L);
        assertThat(pkg.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactlyInAnyOrder(10L, 20L, 30L);
    }

    @Test
    @DisplayName("ActivityPackage 수정 - packageTitle, packageDescription, poId 변경")
    void updateActivityPackage_allFields() {
        ActivityPackage pkg = buildBasicPackage();

        pkg.update(
                "수정된 패키지 제목",
                "수정된 설명",
                "PO-2025-999"
        );

        assertThat(pkg.getPackageTitle()).isEqualTo("수정된 패키지 제목");
        assertThat(pkg.getPackageDescription()).isEqualTo("수정된 설명");
        assertThat(pkg.getPoId()).isEqualTo("PO-2025-999");
    }

    @Test
    @DisplayName("ActivityPackage 수정 - packageDescription, poId null로 클리어")
    void updateActivityPackage_clearOptionalFields() {
        ActivityPackage pkg = buildBasicPackage();

        pkg.update("2025 Q1 영업 활동 패키지", null, null);

        assertThat(pkg.getPackageDescription()).isNull();
        assertThat(pkg.getPoId()).isNull();
    }

    @Test
    @DisplayName("creatorId는 수정 불가 - 패키지 생성자 고정")
    void creatorId_isImmutable() {
        ActivityPackage pkg = buildBasicPackage();

        pkg.update("수정된 제목", null, null);

        assertThat(pkg.getCreatorId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DB - 기본 ActivityPackage 저장 및 조회")
    void db_saveAndFind() {
        ActivityPackage pkg = buildBasicPackage();

        ActivityPackage saved = em.persistFlushFind(pkg);

        assertThat(saved.getPackageId()).isNotNull();
        assertThat(saved.getPackageTitle()).isEqualTo("2025 Q1 영업 활동 패키지");
        assertThat(saved.getCreatorId()).isEqualTo(1L);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("DB - viewers, items cascade 저장 확인")
    void db_saveWithViewersAndItems() {
        ActivityPackage pkg = buildBasicPackage();

        ActivityPackage saved = em.persistFlushFind(pkg);

        assertThat(saved.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactlyInAnyOrder(2L, 3L);
        assertThat(saved.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactlyInAnyOrder(100L, 101L, 102L);
    }

    @Test
    @DisplayName("DB - update() 후 변경사항 DB 반영 확인")
    void db_updatePersists() {
        ActivityPackage pkg = buildBasicPackage();
        ActivityPackage saved = em.persistAndFlush(pkg);

        saved.update("수정된 패키지 제목", "수정된 설명", "PO-2025-999");
        em.flush();
        em.clear();

        ActivityPackage found = em.find(ActivityPackage.class, saved.getPackageId());
        assertThat(found.getPackageTitle()).isEqualTo("수정된 패키지 제목");
        assertThat(found.getPackageDescription()).isEqualTo("수정된 설명");
        assertThat(found.getPoId()).isEqualTo("PO-2025-999");
    }
}
