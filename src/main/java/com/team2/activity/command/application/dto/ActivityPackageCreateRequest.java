package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.ActivityPackageViewer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "활동 패키지 생성 요청")
public record ActivityPackageCreateRequest(
        @Schema(description = "패키지 제목", example = "4월 활동 보고서")
        @NotBlank String packageTitle,
        @Schema(description = "패키지 설명")
        String packageDescription,
        @Schema(description = "PO ID", example = "PO-001")
        String poId,
        @Schema(description = "조회 시작일", example = "2026-04-01")
        LocalDate dateFrom,
        @Schema(description = "조회 종료일", example = "2026-04-30")
        LocalDate dateTo,
        @Schema(description = "포함할 활동 ID 목록")
        List<Long> activityIds,
        @Schema(description = "열람자 사용자 ID 목록")
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
