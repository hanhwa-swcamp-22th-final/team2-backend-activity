package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.ActivityPackageViewer;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record ActivityPackageCreateRequest(
        @NotBlank String packageTitle,
        String packageDescription,
        String poId,
        LocalDate dateFrom,
        LocalDate dateTo,
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
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .creatorId(userId)
                .viewers(viewerList)
                .items(itemList)
                .build();
    }
}
