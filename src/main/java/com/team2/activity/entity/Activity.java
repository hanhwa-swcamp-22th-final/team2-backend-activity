package com.team2.activity.entity;

import com.team2.activity.entity.enums.ActivityType; // 활동 유형 열거형 (MEETING, ISSUE, MEMO, SCHEDULE)
import com.team2.activity.entity.enums.Priority;    // 우선순위 열거형 (HIGH, MEDIUM)
import lombok.Builder; // 빌더 패턴 자동 생성 어노테이션
import lombok.Getter;  // 모든 필드의 getter 메서드 자동 생성 어노테이션

import java.time.LocalDate; // 날짜(시간 없음) 타입 - 활동일, 일정 기간에 사용

// 영업 활동 기록 도메인 객체 (DB 테이블: activities)
@Getter  // 모든 필드의 getter 자동 생성 (getClientId(), getType() 등)
public class Activity {

    private final Long clientId;    // 거래처 ID (생성 후 변경 불가 - final, FK→master.clients)
    private ActivityType type;        // 활동 유형 (미팅/협의, 이슈, 메모/노트, 일정)
    private String title;           // 활동 제목 (필수)
    private String content;         // 활동 내용 상세 (선택)
    private LocalDate date;         // 활동 날짜 (필수)
    private Long authorId;          // 작성자 사용자 ID (FK→auth.users)
    private String poId;            // 연결된 수주건 ID (선택, FK→document.purchase_orders)
    private Priority priority;      // 우선순위 (이슈 타입에서만 사용, 선택)
    private LocalDate scheduleFrom; // 일정 시작일 (일정 타입일 때 필수, 선택)
    private LocalDate scheduleTo;   // 일정 종료일 (일정 타입일 때 필수, 선택)

    @Builder // 이 생성자를 기반으로 빌더 클래스 자동 생성 → Activity.builder().clientId(1L)...build() 가능
    private Activity(Long clientId, ActivityType type, String title, String content,
                     LocalDate date, Long authorId, String poId, Priority priority,
                     LocalDate scheduleFrom, LocalDate scheduleTo) {
        this.clientId = clientId;         // 거래처 ID 초기화
        this.type = type;                 // 활동 유형 초기화
        this.title = title;               // 제목 초기화
        this.content = content;           // 내용 초기화
        this.date = date;                 // 활동 날짜 초기화
        this.authorId = authorId;         // 작성자 ID 초기화
        this.poId = poId;                 // PO ID 초기화
        this.priority = priority;         // 우선순위 초기화
        this.scheduleFrom = scheduleFrom; // 일정 시작일 초기화
        this.scheduleTo = scheduleTo;     // 일정 종료일 초기화
    }

    // 활동 기록 수정 메서드 - clientId는 변경 불가이므로 파라미터에서 제외
    public void update(ActivityType type, String title, String content, LocalDate date,
                       Long authorId, String poId, Priority priority,
                       LocalDate scheduleFrom, LocalDate scheduleTo) {
        this.type = type;                 // 활동 유형 변경
        this.title = title;               // 제목 변경
        this.content = content;           // 내용 변경
        this.date = date;                 // 활동 날짜 변경
        this.authorId = authorId;         // 작성자 ID 변경
        this.poId = poId;                 // PO ID 변경 (null 전달 시 연결 해제)
        this.priority = priority;         // 우선순위 변경 (null 전달 시 제거)
        this.scheduleFrom = scheduleFrom; // 일정 시작일 변경 (null 전달 시 제거)
        this.scheduleTo = scheduleTo;     // 일정 종료일 변경 (null 전달 시 제거)
    }
}
