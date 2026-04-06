package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// 활동 수정 요청 본문을 받는 DTO다.
@Schema(description = "활동 수정 요청")
public record ActivityUpdateRequest(
        @Schema(description = "활동 날짜", example = "2026-04-06")
        @NotNull LocalDate activityDate,
        @Schema(description = "활동 유형")
        @NotNull ActivityType activityType,
        @Schema(description = "활동 제목", example = "고객 미팅 수정")
        @NotBlank String activityTitle,
        @Schema(description = "활동 상세 내용")
        String activityContent,
        @Schema(description = "PO ID", example = "PO-001")
        String poId,
        @Schema(description = "우선순위 (이슈 타입 전용)")
        Priority activityPriority,
        @Schema(description = "일정 시작일", example = "2026-04-06")
        LocalDate activityScheduleFrom,
        @Schema(description = "일정 종료일", example = "2026-04-10")
        LocalDate activityScheduleTo
) {}
