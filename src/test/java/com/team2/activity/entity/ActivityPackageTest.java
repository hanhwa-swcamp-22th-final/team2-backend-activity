package com.team2.activity.entity; // 테스트 대상 클래스와 같은 패키지

import org.junit.jupiter.api.DisplayName; // 테스트 이름 표시 어노테이션
import org.junit.jupiter.api.Test;         // 개별 테스트 메서드 표시 어노테이션

import java.time.LocalDateTime; // 날짜+시간 타입 - 생성/수정 시각
import java.util.List;          // 열람 권한 사용자 ID, 포함 활동기록 ID 목록

import static org.assertj.core.api.Assertions.assertThat; // AssertJ 검증 메서드 정적 import

@DisplayName("ActivityPackage 엔티티 테스트") // 테스트 클래스 전체의 표시 이름
class ActivityPackageTest {

    // ── 공통 픽스처 ────────────────────────────────────────────
    // 여러 테스트에서 반복 사용할 기본 ActivityPackage 객체 생성 헬퍼
    private ActivityPackage buildBasicPackage() {
        return ActivityPackage.builder()
                .title("2025 Q1 영업 활동 패키지")         // 패키지 제목 (필수)
                .description("1분기 주요 거래처 활동 모음") // 패키지 설명 (선택)
                .poId("PO-2025-001")                       // 연결된 수주건 ID (선택)
                .creatorId(1L)                             // 패키지 생성자 사용자 ID (auth 서비스의 user PK)
                .viewerIds(List.of(2L, 3L))                // 열람 권한이 부여된 사용자 ID 목록
                .activityIds(List.of(100L, 101L, 102L))    // 패키지에 포함된 활동기록 ID 목록
                .build();
    }

    // ── 테스트 1: 기본 생성 ────────────────────────────────────
    @Test
    @DisplayName("기본 ActivityPackage 생성 성공")
    void createActivityPackage_basic() {
        ActivityPackage pkg = buildBasicPackage(); // 헬퍼로 기본 패키지 생성

        assertThat(pkg.getTitle()).isEqualTo("2025 Q1 영업 활동 패키지");        // 제목 확인
        assertThat(pkg.getDescription()).isEqualTo("1분기 주요 거래처 활동 모음"); // 설명 확인
        assertThat(pkg.getPoId()).isEqualTo("PO-2025-001");                       // PO ID 확인
        assertThat(pkg.getCreatorId()).isEqualTo(1L);                             // 생성자 ID 확인
        assertThat(pkg.getViewerIds()).containsExactly(2L, 3L);                   // 열람 권한 목록 확인
        assertThat(pkg.getActivityIds()).containsExactly(100L, 101L, 102L);       // 포함 활동 목록 확인
    }

    // ── 테스트 2: 선택 필드 없이 생성 ─────────────────────────
    @Test
    @DisplayName("선택 필드 없이 ActivityPackage 생성")
    void createActivityPackage_withoutOptionalFields() {
        ActivityPackage pkg = ActivityPackage.builder()
                .title("기본 패키지")  // title, creatorId만 필수
                .creatorId(1L)         // 생성자 사용자 ID
                .build();

        assertThat(pkg.getDescription()).isNull();  // 설명 미입력 → null
        assertThat(pkg.getPoId()).isNull();          // PO ID 미입력 → null
    }

    // ── 테스트 3: viewerIds null → 빈 리스트 초기화 ───────────
    @Test
    @DisplayName("viewerIds null 전달 시 빈 리스트로 초기화")
    void createActivityPackage_nullViewerIds() {
        ActivityPackage pkg = ActivityPackage.builder()
                .title("패키지")
                .creatorId(1L)          // 생성자 사용자 ID
                .viewerIds(null)        // null → NPE 방지를 위해 빈 리스트로 초기화해야 함
                .activityIds(null)      // null → 마찬가지로 빈 리스트로 초기화해야 함
                .build();

        assertThat(pkg.getViewerIds()).isNotNull().isEmpty();   // null이 아닌 빈 리스트 확인
        assertThat(pkg.getActivityIds()).isNotNull().isEmpty(); // null이 아닌 빈 리스트 확인
    }

    // ── 테스트 4: viewerIds, activityIds 복수 저장 ────────────
    @Test
    @DisplayName("복수 열람 권한 및 포함 활동기록 저장")
    void createActivityPackage_multipleViewersAndActivities() {
        ActivityPackage pkg = ActivityPackage.builder()
                .title("대용량 패키지")
                .creatorId(1L)                            // 생성자 사용자 ID
                .viewerIds(List.of(2L, 3L, 4L, 5L))      // 4명의 열람 권한 부여
                .activityIds(List.of(10L, 20L, 30L))      // 3개 활동기록 포함
                .build();

        assertThat(pkg.getViewerIds()).hasSize(4).contains(2L, 3L, 4L, 5L); // 4명 열람 권한 확인
        assertThat(pkg.getActivityIds()).hasSize(3).contains(10L, 20L, 30L); // 3개 활동기록 확인
    }

    // ── 테스트 5: update() - 수정 가능 필드 변경 ──────────────
    @Test
    @DisplayName("ActivityPackage 수정 - title, description, poId 변경")
    void updateActivityPackage_allFields() {
        ActivityPackage pkg = buildBasicPackage(); // 기존 패키지

        // update() 메서드로 수정 가능한 필드 변경
        pkg.update(
                "수정된 패키지 제목",  // 제목 변경
                "수정된 설명",         // 설명 변경
                "PO-2025-999"          // PO ID 변경
        );

        assertThat(pkg.getTitle()).isEqualTo("수정된 패키지 제목"); // 제목 변경 확인
        assertThat(pkg.getDescription()).isEqualTo("수정된 설명");  // 설명 변경 확인
        assertThat(pkg.getPoId()).isEqualTo("PO-2025-999");          // PO ID 변경 확인
    }

    // ── 테스트 6: update() - 선택 필드 null로 클리어 ───────────
    @Test
    @DisplayName("ActivityPackage 수정 - description, poId null로 클리어")
    void updateActivityPackage_clearOptionalFields() {
        ActivityPackage pkg = buildBasicPackage(); // description, poId 모두 있는 상태

        // description, poId를 null로 전달하여 제거
        pkg.update("2025 Q1 영업 활동 패키지", null, null);

        assertThat(pkg.getDescription()).isNull(); // 설명 null로 클리어 확인
        assertThat(pkg.getPoId()).isNull();         // PO ID null로 클리어 확인
    }

    // ── 테스트 7: creatorId 불변성 확인 ───────────────────────
    @Test
    @DisplayName("creatorId는 수정 불가 - 패키지 생성자 고정")
    void creatorId_isImmutable() {
        ActivityPackage pkg = buildBasicPackage(); // creatorId=1

        // update()에 creatorId 파라미터가 없으므로 변경 불가
        pkg.update("수정된 제목", null, null);

        assertThat(pkg.getCreatorId()).isEqualTo(1L); // 생성자 ID 불변 확인
    }

    // ── 테스트 8: createdAt 자동 설정 ─────────────────────────
    @Test
    @DisplayName("ActivityPackage 생성 시 createdAt 자동 설정")
    void createActivityPackage_createdAtAutoSet() {
        LocalDateTime before = LocalDateTime.now(); // 생성 직전 시각 기록

        ActivityPackage pkg = ActivityPackage.builder()
                .title("시각 테스트 패키지")
                .creatorId(1L)                   // 생성자 사용자 ID
                .build();

        LocalDateTime after = LocalDateTime.now(); // 생성 직후 시각 기록

        assertThat(pkg.getCreatedAt())
                .isAfterOrEqualTo(before)   // createdAt이 생성 직전 시각 이후인지 확인
                .isBeforeOrEqualTo(after);  // createdAt이 생성 직후 시각 이전인지 확인
    }
}
