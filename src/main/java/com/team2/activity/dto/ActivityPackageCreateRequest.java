package com.team2.activity.dto;

import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.entity.ActivityPackageItem;
import com.team2.activity.entity.ActivityPackageViewer;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ActivityPackageCreateRequest(
        @NotBlank String packageTitle,
        String packageDescription,
        String poId,
        List<Long> activityIds,
        List<Long> viewerIds
) {
    public ActivityPackage toEntity(Long userId) {
        List<ActivityPackageViewer> viewerList = viewerIds != null
                ? viewerIds.stream().map(ActivityPackageViewer::of).toList()
                : List.of();
        List<ActivityPackageItem> itemList = activityIds != null
                ? activityIds.stream().map(ActivityPackageItem::of).toList()
                : List.of();
        return ActivityPackage.builder()
                .packageTitle(packageTitle)
                .packageDescription(packageDescription)
                .poId(poId)
                .creatorId(userId)
                .viewers(viewerList)
                .items(itemList)
                .build();
    }
}
