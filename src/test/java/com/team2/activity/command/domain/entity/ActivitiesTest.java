package com.team2.activity.command.domain.entity;

import com.team2.activity.command.domain.entity.enums.Priority;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Activities 엔티티 테스트")
class ActivitiesTest {

    @Autowired
    private TestEntityManager em;

    private Activity buildBasicRecord() {
        return Activity.builder()
                .clientId(1L)
                .activityAuthorId(10L)
                .activityType(ActivityType.MEETING)
                .activityTitle("거래처 미팅")
                .activityContent("미팅 내용 상세")
                .activityDate(LocalDate.of(2025, 4, 10))
                .build();
    }

    @Test
    @DisplayName("기본 Record 생성")
    void createRecord_basic() {
        Activity record = buildBasicRecord();

        assertThat(record.getClientId()).isEqualTo(1L);
        assertThat(record.getActivityType()).isEqualTo(ActivityType.MEETING);
        assertThat(record.getActivityTitle()).isEqualTo("거래처 미팅");
        assertThat(record.getActivityContent()).isEqualTo("미팅 내용 상세");
        assertThat(record.getActivityDate()).isEqualTo(LocalDate.of(2025, 4, 10));
        assertThat(record.getActivityAuthorId()).isEqualTo(10L);
        assertThat(record.getPoId()).isNull();
        assertThat(record.getActivityPriority()).isNull();
        assertThat(record.getActivityScheduleFrom()).isNull();
        assertThat(record.getActivityScheduleTo()).isNull();
    }

    @Test
    @DisplayName("이슈 타입 Record 생성 - priority 포함")
    void createRecord_issue_withPriority() {
        Activity record = Activity.builder()
                .clientId(1L)
                .activityAuthorId(10L)
                .activityType(ActivityType.ISSUE)
                .activityTitle("긴급 이슈 발생")
                .activityDate(LocalDate.now())
                .activityPriority(Priority.HIGH)
                .build();

        assertThat(record.getActivityType()).isEqualTo(ActivityType.ISSUE);
        assertThat(record.getActivityPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("이슈 보통 우선순위 Record 생성")
    void createRecord_issue_mediumPriority() {
        Activity record = Activity.builder()
                .clientId(1L)
                .activityAuthorId(10L)
                .activityType(ActivityType.ISSUE)
                .activityTitle("일반 이슈")
                .activityDate(LocalDate.now())
                .activityPriority(Priority.MEDIUM)
                .build();

        assertThat(record.getActivityPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    @DisplayName("일정 타입 Record 생성 - scheduleFrom/To 포함")
    void createRecord_schedule_withDateRange() {
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to   = LocalDate.of(2025, 4, 5);

        Activity record = Activity.builder()
                .clientId(1L)
                .activityAuthorId(10L)
                .activityType(ActivityType.SCHEDULE)
                .activityTitle("해외 출장")
                .activityDate(LocalDate.now())
                .activityScheduleFrom(from)
                .activityScheduleTo(to)
                .build();

        assertThat(record.getActivityType()).isEqualTo(ActivityType.SCHEDULE);
        assertThat(record.getActivityScheduleFrom()).isEqualTo(from);
        assertThat(record.getActivityScheduleTo()).isEqualTo(to);
    }

    @Test
    @DisplayName("메모 타입 Record 생성")
    void createRecord_memo() {
        Activity record = Activity.builder()
                .clientId(2L)
                .activityAuthorId(20L)
                .activityType(ActivityType.MEMO)
                .activityTitle("미팅 후 메모")
                .activityContent("논의된 사항 정리")
                .activityDate(LocalDate.now())
                .build();

        assertThat(record.getActivityType()).isEqualTo(ActivityType.MEMO);
        assertThat(record.getActivityContent()).isEqualTo("논의된 사항 정리");
    }

    @Test
    @DisplayName("PO 연결된 Record 생성")
    void createRecord_withPoId() {
        Activity record = Activity.builder()
                .clientId(1L)
                .activityAuthorId(10L)
                .activityType(ActivityType.MEETING)
                .activityTitle("PO 관련 미팅")
                .activityDate(LocalDate.now())
                .poId("PO-2025-001")
                .build();

        assertThat(record.getPoId()).isEqualTo("PO-2025-001");
    }

    @Test
    @DisplayName("Record 수정 - 모든 필드 변경")
    void updateRecord_allFields() {
        Activity record = buildBasicRecord();

        record.update(
                ActivityType.MEMO,
                "수정된 제목",
                "수정된 내용",
                LocalDate.of(2025, 5, 1),
                20L,
                "PO-001",
                null,
                null,
                null
        );

        assertThat(record.getActivityType()).isEqualTo(ActivityType.MEMO);
        assertThat(record.getActivityTitle()).isEqualTo("수정된 제목");
        assertThat(record.getActivityContent()).isEqualTo("수정된 내용");
        assertThat(record.getActivityDate()).isEqualTo(LocalDate.of(2025, 5, 1));
        assertThat(record.getActivityAuthorId()).isEqualTo(20L);
        assertThat(record.getPoId()).isEqualTo("PO-001");
        assertThat(record.getActivityPriority()).isNull();
        assertThat(record.getActivityScheduleFrom()).isNull();
        assertThat(record.getActivityScheduleTo()).isNull();
    }

    @Test
    @DisplayName("Record 수정 - 일정 타입으로 변경")
    void updateRecord_toScheduleType() {
        Activity record = buildBasicRecord();
        LocalDate from = LocalDate.of(2025, 6, 1);
        LocalDate to   = LocalDate.of(2025, 6, 5);

        record.update(
                ActivityType.SCHEDULE,
                "출장 일정",
                "출장 일정 내용",
                LocalDate.now(),
                10L,
                null,
                null,
                from,
                to
        );

        assertThat(record.getActivityType()).isEqualTo(ActivityType.SCHEDULE);
        assertThat(record.getActivityScheduleFrom()).isEqualTo(from);
        assertThat(record.getActivityScheduleTo()).isEqualTo(to);
    }

    @Test
    @DisplayName("clientId는 수정 불가 - 고정 필드")
    void clientId_isImmutable() {
        Activity record = buildBasicRecord();
        assertThat(record.getClientId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ActivityType 열거값 확인")
    void recordType_values() {
        assertThat(ActivityType.values())
                .containsExactlyInAnyOrder(
                        ActivityType.MEETING,
                        ActivityType.ISSUE,
                        ActivityType.MEMO,
                        ActivityType.SCHEDULE
                );
    }

    @Test
    @DisplayName("ActivityType JSON 직렬화 - 영문 displayName 반환")
    void recordType_displayName() {
        assertThat(ActivityType.MEETING.getDisplayName()).isEqualTo("MEETING");   // 구: 미팅/협의
        assertThat(ActivityType.ISSUE.getDisplayName()).isEqualTo("ISSUE");       // 구: 이슈
        assertThat(ActivityType.MEMO.getDisplayName()).isEqualTo("MEMO");         // 구: 메모/노트
        assertThat(ActivityType.SCHEDULE.getDisplayName()).isEqualTo("SCHEDULE"); // 구: 일정
    }

    @Test
    @DisplayName("ActivityType JSON 역직렬화 - 영문 문자열로 생성")
    void recordType_fromDisplayName() {
        assertThat(ActivityType.from("MEETING")).isEqualTo(ActivityType.MEETING);   // 구: 미팅/협의
        assertThat(ActivityType.from("ISSUE")).isEqualTo(ActivityType.ISSUE);       // 구: 이슈
        assertThat(ActivityType.from("MEMO")).isEqualTo(ActivityType.MEMO);         // 구: 메모/노트
        assertThat(ActivityType.from("SCHEDULE")).isEqualTo(ActivityType.SCHEDULE); // 구: 일정
    }

    @Test
    @DisplayName("Priority 열거값 확인")
    void priority_values() {
        assertThat(Priority.values())
                .containsExactlyInAnyOrder(
                        Priority.MEDIUM,
                        Priority.HIGH
                );
    }

    @Test
    @DisplayName("Priority JSON 직렬화 - 영문 displayName 반환")
    void priority_displayName() {
        assertThat(Priority.MEDIUM.getDisplayName()).isEqualTo("MEDIUM"); // 구: 보통
        assertThat(Priority.HIGH.getDisplayName()).isEqualTo("HIGH");     // 구: 높음
    }

    @Test
    @DisplayName("Priority JSON 역직렬화 - 영문 문자열로 생성")
    void priority_fromDisplayName() {
        assertThat(Priority.from("MEDIUM")).isEqualTo(Priority.MEDIUM); // 구: 보통
        assertThat(Priority.from("HIGH")).isEqualTo(Priority.HIGH);     // 구: 높음
    }

    @Test
    @DisplayName("Record 수정 - 이슈 타입으로 변경 (priority 설정)")
    void updateRecord_toIssueType() {
        Activity record = buildBasicRecord();

        record.update(
                ActivityType.ISSUE,
                "긴급 품질 이슈",
                "불량률 증가 관련",
                LocalDate.now(),
                10L,
                null,
                Priority.HIGH,
                null,
                null
        );

        assertThat(record.getActivityType()).isEqualTo(ActivityType.ISSUE);
        assertThat(record.getActivityPriority()).isEqualTo(Priority.HIGH);
        assertThat(record.getActivityScheduleFrom()).isNull();
        assertThat(record.getActivityScheduleTo()).isNull();
    }

    @Test
    @DisplayName("ActivityType.from() - 잘못된 값 전달 시 예외 발생")
    void recordType_fromInvalidValue_throwsException() {
        assertThatThrownBy(() -> ActivityType.from("잘못된값"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Priority.from() - 잘못된 값 전달 시 예외 발생")
    void priority_fromInvalidValue_throwsException() {
        assertThatThrownBy(() -> Priority.from("잘못된값"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("DB - 기본 Activity 저장 및 조회")
    void db_saveAndFind() {
        Activity activity = buildBasicRecord();

        Activity saved = em.persistFlushFind(activity);

        assertThat(saved.getActivityId()).isNotNull();
        assertThat(saved.getActivityTitle()).isEqualTo("거래처 미팅");
        assertThat(saved.getActivityType()).isEqualTo(ActivityType.MEETING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("DB - ActivityType/Priority Enum 컨버터 저장 확인")
    void db_enumConverters() {
        Activity activity = Activity.builder()
                .clientId(1L).activityAuthorId(10L)
                .activityDate(LocalDate.now())
                .activityType(ActivityType.ISSUE)
                .activityTitle("이슈")
                .activityPriority(Priority.HIGH)
                .build();

        Activity saved = em.persistFlushFind(activity);

        assertThat(saved.getActivityType()).isEqualTo(ActivityType.ISSUE);
        assertThat(saved.getActivityPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("DB - update() 후 변경사항 DB 반영 확인")
    void db_updatePersists() {
        Activity activity = buildBasicRecord();
        Activity saved = em.persistAndFlush(activity);

        saved.update(ActivityType.MEMO, "수정된 제목", "수정된 내용",
                LocalDate.of(2025, 5, 1), 10L, null, null, null, null);
        em.flush();
        em.clear();

        Activity found = em.find(Activity.class, saved.getActivityId());
        assertThat(found.getActivityTitle()).isEqualTo("수정된 제목");
        assertThat(found.getActivityType()).isEqualTo(ActivityType.MEMO);
    }
}
