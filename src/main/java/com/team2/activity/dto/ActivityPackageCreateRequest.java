package com.team2.activity.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ActivityPackageCreateRequest(
        @NotBlank String packageTitle,
        String packageDescription,
        String poId,
        List<Long> activityIds,
        List<Long> viewerIds
) {}
