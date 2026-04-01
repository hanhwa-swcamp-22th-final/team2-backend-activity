package com.team2.activity.command.domain.entity;

import com.team2.activity.command.domain.entity.converter.ActivityTypeConverter;
import com.team2.activity.command.domain.entity.converter.PriorityConverter;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 활동 도메인과 activities 테이블을 매핑하는 JPA 엔티티다.
@Entity
// 매핑 대상 테이블 이름을 지정한다.
@Table(name = "activities")
// 읽기 전용 필드 접근을 위한 getter를 생성한다.
@Getter
// JPA 기본 생성자를 보호 수준으로 제공한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {

    // 활동 기본키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    // 거래처 ID다.
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    // 관련 PO ID다.
    @Column(name = "po_id", length = 30)
    private String poId;

    // 활동 작성자 ID다.
    @Column(name = "activity_author_id", nullable = false)
    private Long activityAuthorId;

    // 활동 날짜다.
    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    // 활동 유형 enum을 DB 문자열과 변환해 저장한다.
    @Convert(converter = ActivityTypeConverter.class)
    @Column(name = "activity_type", nullable = false, length = 20)
    private ActivityType activityType;

    // 활동 제목이다.
    @Column(name = "activity_title", nullable = false, length = 100)
    private String activityTitle;

    // 활동 상세 내용이다.
    @Column(name = "activity_content", columnDefinition = "TEXT")
    private String activityContent;

    // 이슈 타입일 때 사용할 우선순위다.
    @Convert(converter = PriorityConverter.class)
    @Column(name = "activity_priority", length = 10)
    private Priority activityPriority;

    // 일정 시작일이다.
    @Column(name = "activity_schedule_from")
    private LocalDate activityScheduleFrom;

    // 일정 종료일이다.
    @Column(name = "activity_schedule_to")
    private LocalDate activityScheduleTo;

    // 생성 시각이다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 마지막 수정 시각이다.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 빌더 기반 생성 로직을 제공한다.
    @Builder
    private Activity(Long activityId, Long clientId, String poId, Long activityAuthorId, LocalDate activityDate,
                     ActivityType activityType, String activityTitle, String activityContent,
                     Priority activityPriority, LocalDate activityScheduleFrom, LocalDate activityScheduleTo) {
        // 빌더에서 받은 활동 ID를 엔티티 기본키 필드에 저장한다.
        this.activityId = activityId;
        // 빌더에서 받은 거래처 ID를 저장한다.
        this.clientId = clientId;
        // 빌더에서 받은 PO ID를 저장한다.
        this.poId = poId;
        // 빌더에서 받은 작성자 ID를 저장한다.
        this.activityAuthorId = activityAuthorId;
        // 빌더에서 받은 활동 날짜를 저장한다.
        this.activityDate = activityDate;
        // 빌더에서 받은 활동 타입을 저장한다.
        this.activityType = activityType;
        // 빌더에서 받은 활동 제목을 저장한다.
        this.activityTitle = activityTitle;
        // 빌더에서 받은 활동 본문을 저장한다.
        this.activityContent = activityContent;
        // 빌더에서 받은 우선순위를 저장한다.
        this.activityPriority = activityPriority;
        // 빌더에서 받은 일정 시작일을 저장한다.
        this.activityScheduleFrom = activityScheduleFrom;
        // 빌더에서 받은 일정 종료일을 저장한다.
        this.activityScheduleTo = activityScheduleTo;
    }

    // 최초 저장 직전에 생성/수정 시각을 현재 시각으로 채운다.
    @PrePersist
    protected void onCreate() {
        // 최초 저장 시 생성 시각을 현재 시각으로 채운다.
        this.createdAt = LocalDateTime.now();
        // 최초 저장 시 수정 시각도 현재 시각으로 맞춘다.
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 직전에 수정 시각을 현재 시각으로 갱신한다.
    @PreUpdate
    protected void onUpdate() {
        // 수정 직전에 수정 시각을 현재 시각으로 갱신한다.
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 가능한 필드들을 요청 값으로 한 번에 갱신한다.
    public void update(ActivityType activityType, String activityTitle, String activityContent,
                       LocalDate activityDate, Long activityAuthorId, String poId,
                       Priority activityPriority, LocalDate activityScheduleFrom, LocalDate activityScheduleTo) {
        // 새 활동 타입으로 값을 교체한다.
        this.activityType = activityType;
        // 새 활동 제목으로 값을 교체한다.
        this.activityTitle = activityTitle;
        // 새 활동 본문으로 값을 교체한다.
        this.activityContent = activityContent;
        // 새 활동 날짜로 값을 교체한다.
        this.activityDate = activityDate;
        // 새 작성자 ID로 값을 교체한다.
        this.activityAuthorId = activityAuthorId;
        // 새 PO ID로 값을 교체한다.
        this.poId = poId;
        // 새 우선순위로 값을 교체한다.
        this.activityPriority = activityPriority;
        // 새 일정 시작일로 값을 교체한다.
        this.activityScheduleFrom = activityScheduleFrom;
        // 새 일정 종료일로 값을 교체한다.
        this.activityScheduleTo = activityScheduleTo;
    }
}
