package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// 활동 생성 요청 본문을 받는 DTO다.
@Schema(description = "활동 생성 요청")
public record ActivityCreateRequest(
        // 활동이 속한 거래처 ID다.
        @Schema(description = "거래처 ID", example = "1")
        @NotNull Long clientId,
        // 연결된 PO 번호다 (선택).
        @Schema(description = "PO 번호", example = "po2026001")
        String poId,
        // 활동이 발생한 날짜다.
        @Schema(description = "활동 날짜", example = "2026-04-06")
        @NotNull LocalDate activityDate,
        // 활동 분류 값이다.
        @Schema(description = "활동 유형")
        @NotNull ActivityType activityType,
        // 활동 제목이다.
        @Schema(description = "활동 제목", example = "고객 미팅")
        @NotBlank String activityTitle,
        // 활동 상세 내용이다 (선택).
        @Schema(description = "활동 상세 내용")
        String activityContent,
        // 우선순위다 (이슈 타입 전용, 선택).
        @Schema(description = "우선순위 (이슈 타입 전용)")
        Priority activityPriority,
        // 활동 일정 시작일이다 (일정 유형일 때만 필수).
        @Schema(description = "일정 시작일", example = "2026-04-06")
        LocalDate activityScheduleFrom,
        // 활동 일정 종료일이다 (일정 유형일 때만 필수).
        @Schema(description = "일정 종료일", example = "2026-04-10")
        LocalDate activityScheduleTo
) {
    // 요청 DTO를 저장 가능한 Activity 엔티티로 변환한다.
    public Activity toEntity(Long userId) {
        return Activity.builder()
                .clientId(clientId)
                .poId(poId)
                .activityDate(activityDate)
                .activityType(activityType)
                .activityTitle(activityTitle)
                .activityContent(activityContent)
                .activityAuthorId(userId)
                .activityPriority(activityPriority)
                .activityScheduleFrom(activityScheduleFrom)
                .activityScheduleTo(activityScheduleTo)
                .build();
    }
}
