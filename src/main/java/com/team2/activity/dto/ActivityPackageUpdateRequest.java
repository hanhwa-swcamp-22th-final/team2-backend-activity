package com.team2.activity.dto;

import java.util.List;

public record ActivityPackageUpdateRequest(
        String packageTitle,
        String packageDescription,
        String poId,
        List<Long> activityIds,
        List<Long> viewerIds
) {}
