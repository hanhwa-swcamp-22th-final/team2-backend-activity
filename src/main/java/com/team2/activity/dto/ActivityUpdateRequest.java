package com.team2.activity.dto;

import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ActivityUpdateRequest(
        @NotNull LocalDate activityDate,
        @NotNull ActivityType activityType,
        @NotBlank String activityTitle,
        String activityContent,
        String poId,
        Priority activityPriority,
        LocalDate activityScheduleFrom,
        LocalDate activityScheduleTo
) {}
