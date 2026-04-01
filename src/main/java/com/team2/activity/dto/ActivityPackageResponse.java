package com.team2.activity.dto;

import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.entity.ActivityPackageItem;
import com.team2.activity.entity.ActivityPackageViewer;

import java.util.List;

public record ActivityPackageResponse(
        Long packageId,
        String packageTitle,
        String packageDescription,
        String poId,
        Long creatorId,
        List<Long> activityIds,
        List<Long> viewerIds
) {
    public static ActivityPackageResponse from(ActivityPackage p) {
        return new ActivityPackageResponse(
                p.getPackageId(),
                p.getPackageTitle(),
                p.getPackageDescription(),
                p.getPoId(),
                p.getCreatorId(),
                p.getItems().stream().map(ActivityPackageItem::getActivityId).toList(),
                p.getViewers().stream().map(ActivityPackageViewer::getUserId).toList()
        );
    }
}
