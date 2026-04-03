package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.ActivityPackageViewer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ActivityPackageResponse(
        Long packageId,
        String packageTitle,
        String packageDescription,
        String poId,
        Long creatorId,
        String creatorName,
        LocalDate dateFrom,
        LocalDate dateTo,
        int activityCount,
        int viewerCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<Long> activityIds,
        List<Long> viewerIds
) {
    public static ActivityPackageResponse from(ActivityPackage p) {
        return from(p, null);
    }

    public static ActivityPackageResponse from(ActivityPackage p, String creatorName) {
        return new ActivityPackageResponse(
                p.getPackageId(),
                p.getPackageTitle(),
                p.getPackageDescription(),
                p.getPoId(),
                p.getCreatorId(),
                creatorName,
                p.getDateFrom(),
                p.getDateTo(),
                p.getItems().size(),
                p.getViewers().size(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getItems().stream().map(ActivityPackageItem::getActivityId).toList(),
                p.getViewers().stream().map(ActivityPackageViewer::getUserId).toList()
        );
    }
}
