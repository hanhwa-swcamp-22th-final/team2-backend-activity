// ActivityPackageRepositoryTest: ActivityPackage 엔티티의 Repository 계층 테스트
package com.team2.activity.repository;

// ActivityPackage 엔티티 import
import com.team2.activity.entity.ActivityPackage;
// ActivityPackageItem 엔티티 import
import com.team2.activity.entity.ActivityPackageItem;
// ActivityPackageViewer 엔티티 import
import com.team2.activity.entity.ActivityPackageViewer;
// 테스트 메서드 표시 import
import org.junit.jupiter.api.DisplayName;
// 테스트 메서드 import
import org.junit.jupiter.api.Test;
// 의존성 주입 import
import org.springframework.beans.factory.annotation.Autowired;
// JPA 테스트 설정 import
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// 테스트 DB 교체 방지 설정 import
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// 테스트 프로파일 활성화 import
import org.springframework.test.context.ActiveProfiles;

// 리스트 타입 import
import java.util.List;

// AssertJ assertion import
import static org.assertj.core.api.Assertions.assertThat;

// JPA 테스트 설정 어노테이션
@DataJpaTest
// application-test.properties의 H2 datasource를 그대로 사용 (교체하지 않음)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// 테스트 프로파일 설정
@ActiveProfiles("test")
// 테스트 클래스 표시명
@DisplayName("ActivityPackageRepository 테스트")
// ActivityPackageRepository 테스트 클래스
class ActivityPackageRepositoryTest {

    // 테스트 대상 Repository 주입
    @Autowired
    private ActivityPackageRepository activityPackageRepository;

    // ── 공통 픽스처 (테스트용 객체 생성 헬퍼 메서드) ─────────────────────────────────────────────
    // 기본 ActivityPackage 객체 생성 헬퍼 메서드
    private ActivityPackage buildPackage(String title, Long creatorId) {
        // ActivityPackage 빌더 패턴으로 객체 생성
        return ActivityPackage.builder()
                // 패키지 제목 설정
                .packageTitle(title)
                // 패키지 생성자 ID 설정
                .creatorId(creatorId)
                // 빌드 완료
                .build();
    }

    // ── 테스트 1: 저장 및 ID 조회 ───────────────────────────────
    // 패키지 저장 후 ID로 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("패키지 저장 후 ID로 조회")
    // 테스트 메서드
    void saveAndFindById() {
        // 기본 패키지 생성 (제목=2025 Q1 영업 패키지, 생성자ID=1)
        ActivityPackage pkg = buildPackage("2025 Q1 영업 패키지", 1L);

        // 패키지 저장
        ActivityPackage saved = activityPackageRepository.save(pkg);

        // PK 자동 생성 확인
        assertThat(saved.getPackageId()).isNotNull();
        // 저장된 ID로 조회하여 제목이 일치하는지 확인
        assertThat(activityPackageRepository.findById(saved.getPackageId()))
                // Optional이 존재하는지 확인
                .isPresent()
                // Optional에서 값 추출
                .get()
                // 패키지 제목 추출
                .extracting(ActivityPackage::getPackageTitle)
                // 제목이 "2025 Q1 영업 패키지"와 일치하는지 확인
                .isEqualTo("2025 Q1 영업 패키지");
    }

    // ── 테스트 2: createdAt 자동 설정 ───────────────────────────
    // 저장 시 생성 일시가 자동으로 설정되는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("저장 시 createdAt 자동 설정")
    // 테스트 메서드
    void save_autoSetCreatedAt() {
        // 기본 패키지 저장 (제목=패키지, 생성자ID=1)
        ActivityPackage saved = activityPackageRepository.save(buildPackage("패키지", 1L));

        // 생성 일시가 자동으로 설정되었는지 확인
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ── 테스트 3: viewer, item 포함하여 저장 ─────────────────────
    // 열람 권한과 활동기록을 포함하여 패키지를 저장하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("열람 권한, 활동기록 포함하여 저장 및 조회")
    // 테스트 메서드
    void saveWithViewersAndItems() {
        // 열람 권한과 활동기록을 포함한 패키지 생성
        ActivityPackage pkg = ActivityPackage.builder()
                // 패키지 제목 설정
                .packageTitle("공유 패키지")
                // 패키지 생성자 ID 설정
                .creatorId(1L)
                // 열람 권한 사용자 리스트 설정
                .viewers(List.of(
                        // 사용자 ID=2에게 열람 권한 부여
                        ActivityPackageViewer.of(2L),
                        // 사용자 ID=3에게 열람 권한 부여
                        ActivityPackageViewer.of(3L)
                ))
                // 포함된 활동기록 리스트 설정
                .items(List.of(
                        // 활동기록 ID=100 포함
                        ActivityPackageItem.of(100L),
                        // 활동기록 ID=101 포함
                        ActivityPackageItem.of(101L)
                ))
                // 빌드 완료
                .build();

        // 패키지 저장
        ActivityPackage saved = activityPackageRepository.save(pkg);

        // 저장된 ID로 조회하여 패키지 반환
        ActivityPackage found = activityPackageRepository.findById(saved.getPackageId()).orElseThrow();
        // 저장된 열람 권한이 2명인지 확인
        assertThat(found.getViewers()).hasSize(2)
                // 열람 권한 사용자 ID 추출
                .extracting(ActivityPackageViewer::getUserId)
                // 2L과 3L이 모두 포함되어 있는지 확인 (순서 무관)
                .containsExactlyInAnyOrder(2L, 3L);
        // 저장된 활동기록이 2개인지 확인
        assertThat(found.getItems()).hasSize(2)
                // 활동기록 ID 추출
                .extracting(ActivityPackageItem::getActivityId)
                // 100L과 101L이 모두 포함되어 있는지 확인 (순서 무관)
                .containsExactlyInAnyOrder(100L, 101L);
    }

    // ── 테스트 4: 선택 필드 null 저장 ───────────────────────────
    // 선택 필드(설명, PO ID)없이 패키지를 저장하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("description, poId null 저장 허용")
    // 테스트 메서드
    void save_withoutOptionalFields() {
        // 필수 필드만 설정하여 패키지 생성
        ActivityPackage pkg = ActivityPackage.builder()
                // 패키지 제목 설정
                .packageTitle("기본 패키지")
                // 패키지 생성자 ID 설정
                .creatorId(1L)
                // 패키지 설명과 PO ID 미설정 (선택 필드)
                // description, poId 미설정
                .build();

        // 패키지 저장
        ActivityPackage saved = activityPackageRepository.save(pkg);

        // 저장된 ID로 조회하여 패키지 반환
        ActivityPackage found = activityPackageRepository.findById(saved.getPackageId()).orElseThrow();
        // 패키지 설명이 null인지 확인
        assertThat(found.getPackageDescription()).isNull();
        // PO ID가 null인지 확인
        assertThat(found.getPoId()).isNull();
    }

    // ── 테스트 5: 삭제 시 viewers, items cascade 삭제 ─────────────
    // 패키지 삭제 시 열람 권한과 활동기록도 함께 삭제되는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("패키지 삭제 시 viewer, item cascade 삭제")
    // 테스트 메서드
    void delete_cascadeViewersAndItems() {
        // 열람 권한과 활동기록을 포함한 패키지 생성
        ActivityPackage pkg = ActivityPackage.builder()
                // 패키지 제목 설정
                .packageTitle("삭제 테스트 패키지")
                // 패키지 생성자 ID 설정
                .creatorId(1L)
                // 열람 권한 사용자 리스트 설정 (사용자 ID=2)
                .viewers(List.of(ActivityPackageViewer.of(2L)))
                // 포함된 활동기록 리스트 설정 (활동기록 ID=100)
                .items(List.of(ActivityPackageItem.of(100L)))
                // 빌드 완료
                .build();
        // 패키지 저장
        ActivityPackage saved = activityPackageRepository.save(pkg);

        // 저장된 패키지 ID로 삭제
        activityPackageRepository.deleteById(saved.getPackageId());

        // 삭제된 패키지가 조회되지 않는지 확인
        assertThat(activityPackageRepository.findById(saved.getPackageId()))
                // Optional이 비어있는지 확인 (cascade 삭제 완료)
                .isEmpty();
    }

    // ── 테스트 6: creatorId로 목록 조회 ─────────────────────────
    // 생성자 ID로 패키지 목록을 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("creatorId로 패키지 목록 조회")
    // 테스트 메서드
    void findAllByCreatorId() {
        // 생성자 ID=1인 패키지1 저장
        activityPackageRepository.save(buildPackage("패키지1", 1L));
        // 생성자 ID=1인 패키지2 저장
        activityPackageRepository.save(buildPackage("패키지2", 1L));
        // 생성자 ID=2인 패키지3 저장 (다른 생성자)
        activityPackageRepository.save(buildPackage("패키지3", 2L));

        // 생성자 ID=1로 패키지 목록 조회
        List<ActivityPackage> result = activityPackageRepository.findAllByCreatorId(1L);

        // 생성자 ID=1인 패키지가 2개인지 확인
        assertThat(result).hasSize(2);
    }
// ActivityPackageRepositoryTest 클래스 종료
}
