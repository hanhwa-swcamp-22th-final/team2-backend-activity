package com.team2.activity.command.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// ActivityPackage 엔티티의 생성, 수정, JPA 영속화 동작을 검증한다.
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ActivityPackage 엔티티 테스트")
class ActivityPackageTest {

    // 엔티티 저장과 재조회에 사용할 JPA 테스트 전용 EntityManager다.
    @Autowired
    private TestEntityManager em;

    // 패키지 엔티티 테스트 전반에서 재사용할 기본 패키지 픽스처다.
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
        // 모든 주요 필드가 채워진 패키지를 생성한다.
        ActivityPackage pkg = buildBasicPackage();

        // 생성 직후 필드 값이 그대로 유지되는지 확인한다.
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
        // 선택 필드를 생략한 패키지를 생성한다.
        ActivityPackage pkg = ActivityPackage.builder()
                .packageTitle("기본 패키지")
                .creatorId(1L)
                .build();

        // 선택 필드는 null로 유지될 수 있어야 한다.
        assertThat(pkg.getPackageDescription()).isNull();
        assertThat(pkg.getPoId()).isNull();
    }

    @Test
    @DisplayName("viewers null 전달 시 빈 리스트로 초기화")
    void createActivityPackage_nullViewers() {
        // null 컬렉션 전달 시 내부 빈 리스트 초기화를 검증한다.
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
        // 여러 열람자와 활동 항목을 가진 패키지를 생성한다.
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

        // 컬렉션 값이 누락 없이 유지되는지 확인한다.
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
        // 기본 패키지에 수정 메서드를 적용한다.
        ActivityPackage pkg = buildBasicPackage();

        pkg.update(
                "수정된 패키지 제목",
                "수정된 설명",
                "PO-2025-999",
                null, null
        );

        // 수정 가능한 본문 필드가 모두 갱신되는지 확인한다.
        assertThat(pkg.getPackageTitle()).isEqualTo("수정된 패키지 제목");
        assertThat(pkg.getPackageDescription()).isEqualTo("수정된 설명");
        assertThat(pkg.getPoId()).isEqualTo("PO-2025-999");
    }

    @Test
    @DisplayName("ActivityPackage 수정 - packageDescription, poId null로 클리어")
    void updateActivityPackage_clearOptionalFields() {
        // 선택 필드를 null로 덮어써서 값이 제거되는지 확인한다.
        ActivityPackage pkg = buildBasicPackage();

        pkg.update("2025 Q1 영업 활동 패키지", null, null, null, null);

        assertThat(pkg.getPackageDescription()).isNull();
        assertThat(pkg.getPoId()).isNull();
    }

    @Test
    @DisplayName("creatorId는 수정 불가 - 패키지 생성자 고정")
    void creatorId_isImmutable() {
        // 수정 호출 이후에도 creatorId는 바뀌지 않아야 한다.
        ActivityPackage pkg = buildBasicPackage();

        pkg.update("수정된 제목", null, null, null, null);

        assertThat(pkg.getCreatorId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DB - 기본 ActivityPackage 저장 및 조회")
    void db_saveAndFind() {
        // 패키지를 저장한 뒤 즉시 다시 조회한다.
        ActivityPackage pkg = buildBasicPackage();

        ActivityPackage saved = em.persistFlushFind(pkg);

        // PK와 감사 필드가 정상 생성되는지 확인한다.
        assertThat(saved.getPackageId()).isNotNull();
        assertThat(saved.getPackageTitle()).isEqualTo("2025 Q1 영업 활동 패키지");
        assertThat(saved.getCreatorId()).isEqualTo(1L);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("DB - viewers, items cascade 저장 확인")
    void db_saveWithViewersAndItems() {
        // 연관 컬렉션이 포함된 패키지를 저장한다.
        ActivityPackage pkg = buildBasicPackage();

        ActivityPackage saved = em.persistFlushFind(pkg);

        // cascade 설정으로 하위 컬렉션까지 저장됐는지 확인한다.
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
        // 저장된 패키지를 수정한 뒤 영속성 컨텍스트를 비운다.
        ActivityPackage pkg = buildBasicPackage();
        ActivityPackage saved = em.persistAndFlush(pkg);

        saved.update("수정된 패키지 제목", "수정된 설명", "PO-2025-999", null, null);
        em.flush();
        em.clear();

        // DB 재조회 결과에 수정 내용이 반영됐는지 확인한다.
        ActivityPackage found = em.find(ActivityPackage.class, saved.getPackageId());
        assertThat(found.getPackageTitle()).isEqualTo("수정된 패키지 제목");
        assertThat(found.getPackageDescription()).isEqualTo("수정된 설명");
        assertThat(found.getPoId()).isEqualTo("PO-2025-999");
    }
}
