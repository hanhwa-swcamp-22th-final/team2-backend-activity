package com.team2.activity.dto;

import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ActivityCreateRequest(
        @NotNull Long clientId,
        @NotNull LocalDate activityDate,
        @NotNull ActivityType activityType,
        @NotBlank String activityTitle
) {
    public Activity toEntity(Long userId) {
        return Activity.builder()
                .clientId(clientId)
                .activityDate(activityDate)
                .activityType(activityType)
                .activityTitle(activityTitle)
                .activityAuthorId(userId)
                .build();
    }
}
