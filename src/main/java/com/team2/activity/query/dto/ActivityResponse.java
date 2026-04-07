package com.team2.activity.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 활동 API 응답에 사용하는 DTO record다.
@Schema(description = "활동 응답")
public record ActivityResponse(
        @Schema(description = "활동 ID") Long activityId,
        @Schema(description = "거래처 ID") Long clientId,
        @Schema(description = "PO ID") String poId,
        @Schema(description = "작성자 ID") Long activityAuthorId,
        @Schema(description = "활동 날짜") LocalDate activityDate,
        @Schema(description = "활동 유형") ActivityType activityType,
        @Schema(description = "활동 제목") String activityTitle,
        @Schema(description = "활동 상세 내용") String activityContent,
        @Schema(description = "우선순위") Priority activityPriority,
        @Schema(description = "일정 시작일") LocalDate activityScheduleFrom,
        @Schema(description = "일정 종료일") LocalDate activityScheduleTo,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "최종 수정 시각") LocalDateTime updatedAt,
        @Schema(description = "작성자 이름") String authorName,
        @Schema(description = "거래처명") String clientName
) {
    // 목록 조회 등 외부 서비스 없이 엔티티만으로 DTO를 생성할 때 사용하는 팩터리 메서드다.
    public static ActivityResponse from(Activity activity) {
        // 이름 필드를 null로 두고 나머지 엔티티 값만 복사한다.
        return from(activity, null, null);
    }

    // 상세 조회 시 작성자 이름과 거래처명을 함께 받아 DTO를 생성하는 팩터리 메서드다.
    public static ActivityResponse from(Activity activity, String authorName, String clientName) {
        // 엔티티 각 필드를 레코드 생성자에 순서대로 넘겨 DTO를 구성한다.
        return new ActivityResponse(
                // 활동 식별자를 복사한다.
                activity.getActivityId(),
                // 거래처 ID를 복사한다.
                activity.getClientId(),
                // PO ID를 복사한다.
                activity.getPoId(),
                // 작성자 ID를 복사한다.
                activity.getActivityAuthorId(),
                // 활동 날짜를 복사한다.
                activity.getActivityDate(),
                // 활동 타입을 복사한다.
                activity.getActivityType(),
                // 활동 제목을 복사한다.
                activity.getActivityTitle(),
                // 활동 본문을 복사한다.
                activity.getActivityContent(),
                // 우선순위를 복사한다.
                activity.getActivityPriority(),
                // 일정 시작일을 복사한다.
                activity.getActivityScheduleFrom(),
                // 일정 종료일을 복사한다.
                activity.getActivityScheduleTo(),
                // 생성 시각을 복사한다.
                activity.getCreatedAt(),
                // 수정 시각을 복사한다.
                activity.getUpdatedAt(),
                // 외부 서비스에서 조회한 작성자 이름을 설정한다.
                authorName,
                // 외부 서비스에서 조회한 거래처명을 설정한다.
                clientName
        );
    }

    @JsonProperty("id")
    public Long id() { return activityId; }

    @JsonProperty("type")
    public ActivityType type() { return activityType; }

    @JsonProperty("title")
    public String title() { return activityTitle; }

    @JsonProperty("date")
    public LocalDate date() { return activityDate; }

    @JsonProperty("author")
    public String author() { return authorName; }

    @JsonProperty("content")
    public String content() { return activityContent; }

    @JsonProperty("priority")
    public Priority priority() { return activityPriority; }
}
