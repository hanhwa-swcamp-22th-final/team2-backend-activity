package com.team2.activity.entity; // 테스트 대상 클래스와 같은 패키지 → package-private 접근 가능

import com.team2.activity.entity.enums.Priority;    // 우선순위 열거형 (MEDIUM, HIGH)
import com.team2.activity.entity.enums.RecordType;  // 기록 유형 열거형 (MEETING, ISSUE, MEMO, SCHEDULE)
import org.junit.jupiter.api.DisplayName;            // 테스트 이름을 한글로 표시하는 어노테이션
import org.junit.jupiter.api.Test;                   // 개별 테스트 메서드 표시 어노테이션

import java.time.LocalDate; // 날짜(시간 없음) 타입

import static org.assertj.core.api.Assertions.assertThat;           // AssertJ 검증 메서드 정적 import
import static org.assertj.core.api.Assertions.assertThatThrownBy; // 예외 검증 메서드 정적 import

@DisplayName("Activities 엔티티 테스트") // 테스트 클래스 전체의 표시 이름
class ActivitiesTest {

    // ── 공통 픽스처 ────────────────────────────────────────────
    // 여러 테스트에서 반복 사용할 기본 Record 객체를 생성하는 헬퍼 메서드
    private Record buildBasicRecord() {
        return Record.builder()          // 빌더 패턴으로 객체 생성 시작
                .clientId(1L)            // 거래처 ID (필수, 마스터 서비스의 client PK)
                .type(RecordType.MEETING) // 기록 유형 = 미팅/협의
                .title("거래처 미팅")     // 기록 제목 (필수)
                .content("미팅 내용 상세") // 상세 내용 (선택)
                .date(LocalDate.of(2025, 4, 10)) // 활동 날짜 (필수)
                .authorId(10L)           // 작성자 사용자 ID (필수, auth 서비스의 user PK)
                .build();               // Record 객체 완성
    }

    // ── 테스트 1: 기본 생성 ────────────────────────────────────
    @Test
    @DisplayName("기본 Record 생성")
    void createRecord_basic() {
        Record record = buildBasicRecord(); // 헬퍼로 기본 Record 생성

        // 각 필드가 빌더에 전달한 값과 일치하는지 검증
        assertThat(record.getClientId()).isEqualTo(1L);                          // 거래처 ID 확인
        assertThat(record.getType()).isEqualTo(RecordType.MEETING);              // 유형 확인
        assertThat(record.getTitle()).isEqualTo("거래처 미팅");                   // 제목 확인
        assertThat(record.getContent()).isEqualTo("미팅 내용 상세");              // 내용 확인
        assertThat(record.getDate()).isEqualTo(LocalDate.of(2025, 4, 10));       // 날짜 확인
        assertThat(record.getAuthorId()).isEqualTo(10L);                         // 작성자 ID 확인
        assertThat(record.getPoId()).isNull();          // PO ID는 선택 필드 → null
        assertThat(record.getPriority()).isNull();      // 우선순위는 이슈 전용 → null
        assertThat(record.getScheduleFrom()).isNull();  // 시작일은 일정 전용 → null
        assertThat(record.getScheduleTo()).isNull();    // 종료일은 일정 전용 → null
    }

    // ── 테스트 2: 이슈 타입 + 높은 우선순위 ───────────────────
    @Test
    @DisplayName("이슈 타입 Record 생성 - priority 포함")
    void createRecord_issue_withPriority() {
        Record record = Record.builder()
                .clientId(1L)
                .type(RecordType.ISSUE)       // 유형 = 이슈
                .title("긴급 이슈 발생")
                .date(LocalDate.now())         // 오늘 날짜
                .authorId(10L)                 // 작성자 사용자 ID
                .priority(Priority.HIGH)       // 우선순위 = 높음 (이슈 타입에서만 의미 있음)
                .build();

        assertThat(record.getType()).isEqualTo(RecordType.ISSUE);       // 유형이 ISSUE인지 확인
        assertThat(record.getPriority()).isEqualTo(Priority.HIGH);      // 우선순위가 HIGH인지 확인
    }

    // ── 테스트 3: 이슈 타입 + 보통 우선순위 ───────────────────
    @Test
    @DisplayName("이슈 보통 우선순위 Record 생성")
    void createRecord_issue_mediumPriority() {
        Record record = Record.builder()
                .clientId(1L)
                .type(RecordType.ISSUE)
                .title("일반 이슈")
                .date(LocalDate.now())
                .authorId(10L)                 // 작성자 사용자 ID
                .priority(Priority.MEDIUM)     // 우선순위 = 보통
                .build();

        assertThat(record.getPriority()).isEqualTo(Priority.MEDIUM); // 보통 우선순위 확인
    }

    // ── 테스트 4: 일정 타입 + 기간 ────────────────────────────
    @Test
    @DisplayName("일정 타입 Record 생성 - scheduleFrom/To 포함")
    void createRecord_schedule_withDateRange() {
        LocalDate from = LocalDate.of(2025, 4, 1); // 일정 시작일
        LocalDate to   = LocalDate.of(2025, 4, 5); // 일정 종료일

        Record record = Record.builder()
                .clientId(1L)
                .type(RecordType.SCHEDULE)     // 유형 = 일정
                .title("해외 출장")
                .date(LocalDate.now())
                .authorId(10L)                 // 작성자 사용자 ID
                .scheduleFrom(from)            // 일정 시작일 설정
                .scheduleTo(to)                // 일정 종료일 설정
                .build();

        assertThat(record.getType()).isEqualTo(RecordType.SCHEDULE);    // 유형이 SCHEDULE인지 확인
        assertThat(record.getScheduleFrom()).isEqualTo(from);           // 시작일 일치 확인
        assertThat(record.getScheduleTo()).isEqualTo(to);               // 종료일 일치 확인
    }

    // ── 테스트 5: 메모 타입 ────────────────────────────────────
    @Test
    @DisplayName("메모 타입 Record 생성")
    void createRecord_memo() {
        Record record = Record.builder()
                .clientId(2L)
                .type(RecordType.MEMO)         // 유형 = 메모/노트
                .title("미팅 후 메모")
                .content("논의된 사항 정리")   // 메모 내용 입력
                .date(LocalDate.now())
                .authorId(20L)                 // 작성자 사용자 ID
                .build();

        assertThat(record.getType()).isEqualTo(RecordType.MEMO);            // 유형 확인
        assertThat(record.getContent()).isEqualTo("논의된 사항 정리");       // 내용 확인
    }

    // ── 테스트 6: PO 연결 ──────────────────────────────────────
    @Test
    @DisplayName("PO 연결된 Record 생성")
    void createRecord_withPoId() {
        Record record = Record.builder()
                .clientId(1L)
                .type(RecordType.MEETING)
                .title("PO 관련 미팅")
                .date(LocalDate.now())
                .authorId(10L)                // 작성자 사용자 ID
                .poId("PO-2025-001")           // 수주건(PO) ID 연결 (documents 서비스의 PO 참조)
                .build();

        assertThat(record.getPoId()).isEqualTo("PO-2025-001"); // PO ID 저장 확인
    }

    // ── 테스트 7: update() - 전체 필드 변경 ───────────────────
    @Test
    @DisplayName("Record 수정 - 모든 필드 변경")
    void updateRecord_allFields() {
        Record record = buildBasicRecord(); // 기존 레코드 (MEETING 타입)

        // update() 메서드로 모든 수정 가능 필드를 한 번에 변경
        record.update(
                RecordType.MEMO,             // 유형 변경: MEETING → MEMO
                "수정된 제목",               // 제목 변경
                "수정된 내용",               // 내용 변경
                LocalDate.of(2025, 5, 1),   // 날짜 변경
                20L,                         // 작성자 ID 변경 (auth 서비스의 user PK)
                "PO-001",                    // PO ID 추가
                null,                        // 우선순위 없음 (MEMO 타입이므로)
                null,                        // 일정 시작일 없음
                null                         // 일정 종료일 없음
        );

        assertThat(record.getType()).isEqualTo(RecordType.MEMO);            // 유형 변경 확인
        assertThat(record.getTitle()).isEqualTo("수정된 제목");              // 제목 변경 확인
        assertThat(record.getContent()).isEqualTo("수정된 내용");            // 내용 변경 확인
        assertThat(record.getDate()).isEqualTo(LocalDate.of(2025, 5, 1));   // 날짜 변경 확인
        assertThat(record.getAuthorId()).isEqualTo(20L);                     // 작성자 ID 변경 확인
        assertThat(record.getPoId()).isEqualTo("PO-001");                    // PO ID 변경 확인
        assertThat(record.getPriority()).isNull();                            // 우선순위 null 확인
        assertThat(record.getScheduleFrom()).isNull();                        // 시작일 null 확인
        assertThat(record.getScheduleTo()).isNull();                          // 종료일 null 확인
    }

    // ── 테스트 8: update() - 일정 타입으로 변경 ───────────────
    @Test
    @DisplayName("Record 수정 - 일정 타입으로 변경")
    void updateRecord_toScheduleType() {
        Record record = buildBasicRecord();               // 기존 레코드 (MEETING)
        LocalDate from = LocalDate.of(2025, 6, 1);       // 새 일정 시작일
        LocalDate to   = LocalDate.of(2025, 6, 5);       // 새 일정 종료일

        record.update(
                RecordType.SCHEDULE, // 유형을 SCHEDULE로 변경
                "출장 일정",
                "출장 일정 내용",
                LocalDate.now(),
                10L,    // 작성자 ID
                null,   // PO 없음
                null,   // 우선순위 없음
                from,   // 일정 시작일 설정
                to      // 일정 종료일 설정
        );

        assertThat(record.getType()).isEqualTo(RecordType.SCHEDULE);  // SCHEDULE로 변경됐는지 확인
        assertThat(record.getScheduleFrom()).isEqualTo(from);          // 시작일 저장 확인
        assertThat(record.getScheduleTo()).isEqualTo(to);              // 종료일 저장 확인
    }

    // ── 테스트 9: clientId 불변성 확인 ────────────────────────
    @Test
    @DisplayName("clientId는 수정 불가 - 고정 필드")
    void clientId_isImmutable() {
        Record record = buildBasicRecord();
        // clientId has no setter - verified by builder pattern
        // clientId는 생성 후 변경 불가 → update() 파라미터에 없음
        assertThat(record.getClientId()).isEqualTo(1L); // 생성 시 값 그대로 유지 확인
    }

    // ── 테스트 10: RecordType 열거값 목록 확인 ────────────────
    @Test
    @DisplayName("RecordType 열거값 확인")
    void recordType_values() {
        // 프론트엔드에서 사용하는 4가지 유형이 모두 정의되어 있는지 검증
        assertThat(RecordType.values())
                .containsExactlyInAnyOrder(
                        RecordType.MEETING,  // 미팅/협의
                        RecordType.ISSUE,    // 이슈
                        RecordType.MEMO,     // 메모/노트
                        RecordType.SCHEDULE  // 일정
                );
    }

    // ── 테스트 11: RecordType JSON 직렬화 (영문 → 한글) ───────
    @Test
    @DisplayName("RecordType JSON 직렬화 - 한글 displayName 반환")
    void recordType_displayName() {
        // @JsonValue 가 붙은 getDisplayName()이 JSON 응답에 한글 문자열을 반환하는지 확인
        assertThat(RecordType.MEETING.getDisplayName()).isEqualTo("미팅/협의"); // 프론트 표시값
        assertThat(RecordType.ISSUE.getDisplayName()).isEqualTo("이슈");
        assertThat(RecordType.MEMO.getDisplayName()).isEqualTo("메모/노트");
        assertThat(RecordType.SCHEDULE.getDisplayName()).isEqualTo("일정");
    }

    // ── 테스트 12: RecordType JSON 역직렬화 (한글 → 영문) ─────
    @Test
    @DisplayName("RecordType JSON 역직렬화 - 한글 문자열로 생성")
    void recordType_fromDisplayName() {
        // 프론트에서 한글 문자열로 전송한 값을 @JsonCreator 로 enum 상수로 변환하는지 확인
        assertThat(RecordType.from("미팅/협의")).isEqualTo(RecordType.MEETING);
        assertThat(RecordType.from("이슈")).isEqualTo(RecordType.ISSUE);
        assertThat(RecordType.from("메모/노트")).isEqualTo(RecordType.MEMO);
        assertThat(RecordType.from("일정")).isEqualTo(RecordType.SCHEDULE);
    }

    // ── 테스트 13: Priority 열거값 목록 확인 ─────────────────────
    @Test
    @DisplayName("Priority 열거값 확인")
    void priority_values() {
        // 시스템에서 사용하는 2가지 우선순위가 모두 정의되어 있는지 검증
        assertThat(Priority.values())
                .containsExactlyInAnyOrder(
                        Priority.MEDIUM, // 보통
                        Priority.HIGH    // 높음
                );
    }

    // ── 테스트 14: Priority JSON 직렬화 (한글) ─────────────────
    @Test
    @DisplayName("Priority JSON 직렬화 - 한글 displayName 반환")
    void priority_displayName() {
        // 프론트에서 priority를 한글("보통", "높음")로 사용하므로 그대로 반환해야 함
        assertThat(Priority.MEDIUM.getDisplayName()).isEqualTo("보통"); // 보통 우선순위 표시값
        assertThat(Priority.HIGH.getDisplayName()).isEqualTo("높음");   // 높은 우선순위 표시값
    }

    // ── 테스트 15: Priority JSON 역직렬화 (한글 → 영문) ─────────
    @Test
    @DisplayName("Priority JSON 역직렬화 - 한글 문자열로 생성")
    void priority_fromDisplayName() {
        // 프론트에서 한글 문자열로 전송한 값을 @JsonCreator 로 enum 상수로 변환하는지 확인
        assertThat(Priority.from("보통")).isEqualTo(Priority.MEDIUM); // "보통" → MEDIUM
        assertThat(Priority.from("높음")).isEqualTo(Priority.HIGH);   // "높음" → HIGH
    }

    // ── 테스트 16: update() - 이슈 타입으로 변경 ─────────────────
    @Test
    @DisplayName("Record 수정 - 이슈 타입으로 변경 (priority 설정)")
    void updateRecord_toIssueType() {
        Record record = buildBasicRecord(); // 기존 레코드 (MEETING 타입)

        record.update(
                RecordType.ISSUE,            // 유형 변경: MEETING → ISSUE
                "긴급 품질 이슈",            // 제목 변경
                "불량률 증가 관련",          // 내용 변경
                LocalDate.now(),             // 날짜 변경
                10L,                         // 작성자 ID
                null,                        // PO 없음
                Priority.HIGH,               // 우선순위 = 높음 (이슈 타입이므로 설정)
                null,                        // 일정 시작일 없음
                null                         // 일정 종료일 없음
        );

        assertThat(record.getType()).isEqualTo(RecordType.ISSUE);     // ISSUE로 변경 확인
        assertThat(record.getPriority()).isEqualTo(Priority.HIGH);    // 우선순위 HIGH 확인
        assertThat(record.getScheduleFrom()).isNull();                 // 일정 시작일 없음 확인
        assertThat(record.getScheduleTo()).isNull();                   // 일정 종료일 없음 확인
    }

    // ── 테스트 17: RecordType.from() 잘못된 값 예외 ──────────────
    @Test
    @DisplayName("RecordType.from() - 잘못된 값 전달 시 예외 발생")
    void recordType_fromInvalidValue_throwsException() {
        // from() 내부 for 루프의 "일치하는 값 없음" 브랜치 커버 → JaCoCo 브랜치 100% 달성
        assertThatThrownBy(() -> RecordType.from("잘못된값"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── 테스트 18: Priority.from() 잘못된 값 예외 ────────────────
    @Test
    @DisplayName("Priority.from() - 잘못된 값 전달 시 예외 발생")
    void priority_fromInvalidValue_throwsException() {
        // from() 내부 for 루프의 "일치하는 값 없음" 브랜치 커버 → JaCoCo 브랜치 100% 달성
        assertThatThrownBy(() -> Priority.from("잘못된값"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
