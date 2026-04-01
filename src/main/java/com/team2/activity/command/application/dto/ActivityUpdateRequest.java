package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// 활동 수정 요청 본문을 받는 DTO다.
public record ActivityUpdateRequest(
        // 수정할 활동 날짜다.
        @NotNull LocalDate activityDate,
        // 수정할 활동 타입이다.
        @NotNull ActivityType activityType,
        // 수정할 활동 제목이다.
        @NotBlank String activityTitle,
        // 수정할 활동 본문이다.
        String activityContent,
        // 수정할 PO ID다.
        String poId,
        // 이슈 타입일 때 사용할 우선순위다.
        Priority activityPriority,
        // 일정 타입일 때 사용할 시작일이다.
        LocalDate activityScheduleFrom,
        // 일정 타입일 때 사용할 종료일이다.
        LocalDate activityScheduleTo
) {}
