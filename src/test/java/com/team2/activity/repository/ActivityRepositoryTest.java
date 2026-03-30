// ActivityRepositoryTest: Activity 엔티티의 Repository 계층 테스트
package com.team2.activity.repository;

// Activity 엔티티 import
import com.team2.activity.entity.Activity;
// ActivityType 열거형 import
import com.team2.activity.entity.enums.ActivityType;
// Priority 열거형 import
import com.team2.activity.entity.enums.Priority;
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
// 페이징 결과 import
import org.springframework.data.domain.Page;
// 페이징 요청 import
import org.springframework.data.domain.PageRequest;
// 동적 쿼리 필터링 import
import org.springframework.data.jpa.domain.Specification;
// 테스트 프로파일 활성화 import
import org.springframework.test.context.ActiveProfiles;

// 날짜 타입 import
import java.time.LocalDate;

// AssertJ assertion import
import static org.assertj.core.api.Assertions.assertThat;

// JPA 테스트 설정 어노테이션
@DataJpaTest
// application-test.properties의 H2 datasource를 그대로 사용 (교체하지 않음)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// 테스트 프로파일 설정
@ActiveProfiles("test")
// 테스트 클래스 표시명
@DisplayName("ActivityRepository 테스트")
// ActivityRepository 테스트 클래스
class ActivityRepositoryTest {

    // 테스트 대상 Repository 주입
    @Autowired
    private ActivityRepository activityRepository;

    // ── 공통 픽스처 (테스트용 객체 생성 헬퍼 메서드) ─────────────────────────────────────────────
    // 기본 Activity 객체 생성 헬퍼 메서드
    private Activity buildActivity(Long clientId, ActivityType type, String title) {
        // Activity 빌더 패턴으로 객체 생성
        return Activity.builder()
                // 거래처 ID 설정
                .clientId(clientId)
                // 활동 작성자 ID 설정 (고정값 10L)
                .activityAuthorId(10L)
                // 활동 날짜 설정 (고정값 2025-04-10)
                .activityDate(LocalDate.of(2025, 4, 10))
                // 활동 타입 설정
                .activityType(type)
                // 활동 제목 설정
                .activityTitle(title)
                // 빌드 완료
                .build();
    }

    // ── 테스트 1: 저장 및 ID 조회 ───────────────────────────────
    // 활동 저장 후 ID로 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("활동 저장 후 ID로 조회")
    // 테스트 메서드
    void saveAndFindById() {
        // 기본 활동 객체 생성 (거래처ID=1, 타입=MEETING, 제목=거래처 미팅)
        Activity activity = buildActivity(1L, ActivityType.MEETING, "거래처 미팅");

        // 활동 객체 저장
        Activity saved = activityRepository.save(activity);

        // PK 자동 생성 확인
        assertThat(saved.getActivityId()).isNotNull();
        // 저장된 ID로 조회하여 제목이 일치하는지 확인
        assertThat(activityRepository.findById(saved.getActivityId()))
                // Optional이 존재하는지 확인
                .isPresent()
                // Optional에서 값 추출
                .get()
                // 활동 제목 추출
                .extracting(Activity::getActivityTitle)
                // 제목이 "거래처 미팅"과 일치하는지 확인
                .isEqualTo("거래처 미팅");
    }

    // ── 테스트 2: createdAt, updatedAt 자동 설정 ────────────────
    // 저장 시 타임스탬프 자동 설정 테스트
    @Test
    // 테스트 표시명
    @DisplayName("저장 시 createdAt, updatedAt 자동 설정")
    // 테스트 메서드
    void save_autoSetTimestamps() {
        // 기본 활동 객체 생성 (거래처ID=1, 타입=MEMO, 제목=메모)
        Activity activity = buildActivity(1L, ActivityType.MEMO, "메모");

        // 활동 객체 저장
        Activity saved = activityRepository.save(activity);

        // 생성 일시가 자동으로 설정되었는지 확인
        assertThat(saved.getCreatedAt()).isNotNull();
        // 수정 일시가 자동으로 설정되었는지 확인
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    // ── 테스트 3: clientId 필터 조회 (페이징) ───────────────────
    // 거래처 ID로 활동 목록을 페이징하여 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("clientId로 활동 목록 페이징 조회")
    // 테스트 메서드
    void findAll_filterByClientId() {
        // 거래처 ID=1, 타입=MEETING인 활동 저장
        activityRepository.save(buildActivity(1L, ActivityType.MEETING, "미팅1"));
        // 거래처 ID=1, 타입=ISSUE인 활동 저장
        activityRepository.save(buildActivity(1L, ActivityType.ISSUE, "이슈1"));
        // 거래처 ID=2, 타입=MEMO인 활동 저장 (다른 거래처)
        activityRepository.save(buildActivity(2L, ActivityType.MEMO, "메모1"));

        // 거래처 ID=1 필터 스펙 생성
        Specification<Activity> spec = ActivitySpecification.withClientId(1L);
        // 필터 스펙과 페이징 정보(0번째 페이지, 10개)로 조회
        Page<Activity> result = activityRepository.findAll(spec, PageRequest.of(0, 10));

        // 거래처 ID=1인 활동이 2개인지 확인
        assertThat(result.getTotalElements()).isEqualTo(2);
        // 조회된 활동들의 거래처 ID가 모두 1L인지 확인
        assertThat(result.getContent())
                // 거래처 ID 추출
                .extracting(Activity::getClientId)
                // 모두 1L인지 확인
                .containsOnly(1L);
    }

    // ── 테스트 4: activityType 필터 조회 ─────────────────────────
    // 활동 타입으로 활동 목록을 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("activityType으로 활동 목록 조회")
    // 테스트 메서드
    void findAll_filterByActivityType() {
        // MEETING 타입 활동 저장
        activityRepository.save(buildActivity(1L, ActivityType.MEETING, "미팅"));
        // ISSUE 타입 활동 저장
        activityRepository.save(buildActivity(1L, ActivityType.ISSUE, "이슈"));
        // ISSUE 타입 활동 저장 (2번째)
        activityRepository.save(buildActivity(1L, ActivityType.ISSUE, "이슈2"));

        // ActivityType=ISSUE 필터 스펙 생성
        Specification<Activity> spec = ActivitySpecification.withActivityType(ActivityType.ISSUE);
        // 필터 스펙과 페이징 정보로 조회
        Page<Activity> result = activityRepository.findAll(spec, PageRequest.of(0, 10));

        // ISSUE 타입인 활동이 2개인지 확인
        assertThat(result.getTotalElements()).isEqualTo(2);
        // 조회된 활동들의 타입이 모두 ISSUE인지 확인
        assertThat(result.getContent())
                // 활동 타입 추출
                .extracting(Activity::getActivityType)
                // 모두 ISSUE인지 확인
                .containsOnly(ActivityType.ISSUE);
    }

    // ── 테스트 5: 날짜 범위 필터 조회 ───────────────────────────
    // 활동 날짜 범위로 활동을 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("활동일 범위로 조회 (activity_date_from ~ activity_date_to)")
    // 테스트 메서드
    void findAll_filterByDateRange() {
        // 3월 1일 MEETING 활동 생성
        Activity a1 = Activity.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 작성자 ID 설정
                .activityAuthorId(10L)
                // 활동 날짜 설정 (2025-03-01)
                .activityDate(LocalDate.of(2025, 3, 1))
                // 활동 타입 설정
                .activityType(ActivityType.MEETING)
                // 활동 제목 설정
                .activityTitle("3월 미팅")
                // 빌드 완료
                .build();
        // 4월 15일 MEETING 활동 생성
        Activity a2 = Activity.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 작성자 ID 설정
                .activityAuthorId(10L)
                // 활동 날짜 설정 (2025-04-15)
                .activityDate(LocalDate.of(2025, 4, 15))
                // 활동 타입 설정
                .activityType(ActivityType.MEETING)
                // 활동 제목 설정
                .activityTitle("4월 미팅")
                // 빌드 완료
                .build();
        // 5월 20일 MEETING 활동 생성
        Activity a3 = Activity.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 작성자 ID 설정
                .activityAuthorId(10L)
                // 활동 날짜 설정 (2025-05-20)
                .activityDate(LocalDate.of(2025, 5, 20))
                // 활동 타입 설정
                .activityType(ActivityType.MEETING)
                // 활동 제목 설정
                .activityTitle("5월 미팅")
                // 빌드 완료
                .build();
        // 3월 활동 저장
        activityRepository.save(a1);
        // 4월 활동 저장
        activityRepository.save(a2);
        // 5월 활동 저장
        activityRepository.save(a3);

        // 2025-04-01부터 2025-04-30까지 날짜 범위 필터 스펙 생성
        Specification<Activity> spec = ActivitySpecification.withDateRange(
                // 시작 날짜 설정
                LocalDate.of(2025, 4, 1),
                // 종료 날짜 설정
                LocalDate.of(2025, 4, 30)
        );
        // 필터 스펙과 페이징 정보로 조회
        Page<Activity> result = activityRepository.findAll(spec, PageRequest.of(0, 10));

        // 4월 범위 내 활동이 1개인지 확인
        assertThat(result.getTotalElements()).isEqualTo(1);
        // 조회된 활동의 제목이 "4월 미팅"인지 확인
        assertThat(result.getContent()
                // 첫 번째 활동 추출
                .get(0)
                // 활동 제목 추출
                .getActivityTitle())
                // 제목이 "4월 미팅"과 일치하는지 확인
                .isEqualTo("4월 미팅");
    }

    // ── 테스트 6: authorId 필터 조회 ────────────────────────────
    // 작성자 ID로 활동 목록을 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("작성자 ID로 활동 목록 조회")
    // 테스트 메서드
    void findAll_filterByAuthorId() {
        // 작성자 ID=10인 MEETING 활동 생성 (buildActivity는 activityAuthorId=10L 고정)
        Activity a1 = buildActivity(1L, ActivityType.MEETING, "작성자10");
        // 작성자 ID=20인 MEMO 활동 생성 (별도 builder 사용, activityAuthorId 다름)
        Activity a2 = Activity.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 작성자 ID 설정 (20L, buildActivity 기본값 10L과 다름)
                .activityAuthorId(20L)
                // 활동 날짜 설정 (buildActivity와 동일한 날짜 사용)
                .activityDate(LocalDate.of(2025, 4, 10))
                // 활동 타입 설정
                .activityType(ActivityType.MEMO)
                // 활동 제목 설정
                .activityTitle("작성자20")
                // 빌드 완료
                .build();
        // 첫 번째 활동 저장
        activityRepository.save(a1);
        // 두 번째 활동 저장
        activityRepository.save(a2);

        // 작성자 ID=10 필터 스펙 생성
        Specification<Activity> spec = ActivitySpecification.withAuthorId(10L);
        // 필터 스펙과 페이징 정보로 조회
        Page<Activity> result = activityRepository.findAll(spec, PageRequest.of(0, 10));

        // 작성자 ID=10인 활동이 1개인지 확인
        assertThat(result.getTotalElements()).isEqualTo(1);
        // 조회된 활동의 작성자 ID가 10L인지 확인
        assertThat(result.getContent()
                // 첫 번째 활동 추출
                .get(0)
                // 작성자 ID 추출
                .getActivityAuthorId())
                // 작성자 ID가 10L과 일치하는지 확인
                .isEqualTo(10L);
    }

    // ── 테스트 7: 복합 조건 조회 ────────────────────────────────
    // 거래처 ID와 활동 타입 복합 조건으로 활동을 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("clientId + activityType 복합 조건으로 조회")
    // 테스트 메서드
    void findAll_combinedFilters() {
        // 거래처 ID=1, 타입=ISSUE인 활동 저장
        activityRepository.save(buildActivity(1L, ActivityType.ISSUE, "거래처1 이슈"));
        // 거래처 ID=1, 타입=MEETING인 활동 저장
        activityRepository.save(buildActivity(1L, ActivityType.MEETING, "거래처1 미팅"));
        // 거래처 ID=2, 타입=ISSUE인 활동 저장
        activityRepository.save(buildActivity(2L, ActivityType.ISSUE, "거래처2 이슈"));

        // 거래처 ID=1 필터 스펙 생성
        Specification<Activity> spec = ActivitySpecification.withClientId(1L)
                // AND 연산자로 활동 타입=ISSUE 필터 추가
                .and(ActivitySpecification.withActivityType(ActivityType.ISSUE));
        // 복합 필터 스펙과 페이징 정보로 조회
        Page<Activity> result = activityRepository.findAll(spec, PageRequest.of(0, 10));

        // 거래처 ID=1이면서 타입=ISSUE인 활동이 1개인지 확인
        assertThat(result.getTotalElements()).isEqualTo(1);
        // 조회된 활동의 제목이 "거래처1 이슈"인지 확인
        assertThat(result.getContent()
                // 첫 번째 활동 추출
                .get(0)
                // 활동 제목 추출
                .getActivityTitle())
                // 제목이 "거래처1 이슈"과 일치하는지 확인
                .isEqualTo("거래처1 이슈");
    }

    // ── 테스트 8: 이슈 타입 priority 저장 ───────────────────────
    // 이슈 타입 활동의 우선순위를 저장 및 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("이슈 타입 활동 priority 저장 및 조회")
    // 테스트 메서드
    void save_issuePriority() {
        // ISSUE 타입의 활동 생성
        Activity activity = Activity.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 작성자 ID 설정
                .activityAuthorId(10L)
                // 활동 날짜 설정 (현재 날짜)
                .activityDate(LocalDate.now())
                // 활동 타입 설정 (ISSUE)
                .activityType(ActivityType.ISSUE)
                // 활동 제목 설정
                .activityTitle("긴급 이슈")
                // 우선순위 설정 (HIGH)
                .activityPriority(Priority.HIGH)
                // 빌드 완료
                .build();

        // 활동 저장
        Activity saved = activityRepository.save(activity);

        // 저장된 ID로 조회하여 우선순위가 HIGH인지 확인
        assertThat(activityRepository.findById(saved.getActivityId()))
                // Optional이 존재하는지 확인
                .isPresent()
                // Optional에서 값 추출
                .get()
                // 우선순위 추출
                .extracting(Activity::getActivityPriority)
                // 우선순위가 HIGH와 일치하는지 확인
                .isEqualTo(Priority.HIGH);
    }

    // ── 테스트 9: 일정 타입 schedule 저장 ───────────────────────
    // 일정 타입 활동의 일정 기간을 저장 및 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("일정 타입 활동 scheduleFrom/To 저장 및 조회")
    // 테스트 메서드
    void save_scheduleActivity() {
        // SCHEDULE 타입의 활동 생성
        Activity activity = Activity.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 작성자 ID 설정
                .activityAuthorId(10L)
                // 활동 날짜 설정 (현재 날짜)
                .activityDate(LocalDate.now())
                // 활동 타입 설정 (SCHEDULE)
                .activityType(ActivityType.SCHEDULE)
                // 활동 제목 설정
                .activityTitle("해외 출장")
                // 일정 시작 날짜 설정 (2025-06-01)
                .activityScheduleFrom(LocalDate.of(2025, 6, 1))
                // 일정 종료 날짜 설정 (2025-06-05)
                .activityScheduleTo(LocalDate.of(2025, 6, 5))
                // 빌드 완료
                .build();

        // 활동 저장
        Activity saved = activityRepository.save(activity);

        // 저장된 ID로 조회하여 활동 반환
        Activity found = activityRepository.findById(saved.getActivityId()).orElseThrow();
        // 일정 시작 날짜가 2025-06-01과 일치하는지 확인
        assertThat(found.getActivityScheduleFrom()).isEqualTo(LocalDate.of(2025, 6, 1));
        // 일정 종료 날짜가 2025-06-05와 일치하는지 확인
        assertThat(found.getActivityScheduleTo()).isEqualTo(LocalDate.of(2025, 6, 5));
    }

    // ── 테스트 10: 삭제 ─────────────────────────────────────────
    // 활동을 삭제하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("활동 삭제")
    // 테스트 메서드
    void deleteById() {
        // MEMO 타입 활동 저장
        Activity saved = activityRepository.save(buildActivity(1L, ActivityType.MEMO, "삭제할 메모"));

        // 저장된 활동 ID로 삭제
        activityRepository.deleteById(saved.getActivityId());

        // 삭제된 활동이 조회되지 않는지 확인
        assertThat(activityRepository.findById(saved.getActivityId()))
                // Optional이 비어있는지 확인
                .isEmpty();
    }
// ActivityRepositoryTest 클래스 종료
}
