package com.team2.activity.command.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public record ActivityPackageUpdateRequest(
        @NotBlank String packageTitle,
        String packageDescription,
        String poId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<Long> activityIds,
        List<Long> viewerIds
) {}
