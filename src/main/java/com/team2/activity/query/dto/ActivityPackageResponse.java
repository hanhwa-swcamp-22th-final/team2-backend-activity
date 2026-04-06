package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.ActivityPackageViewer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "활동 패키지 응답")
public record ActivityPackageResponse(
        @Schema(description = "패키지 ID") Long packageId,
        @Schema(description = "패키지 제목") String packageTitle,
        @Schema(description = "패키지 설명") String packageDescription,
        @Schema(description = "PO ID") String poId,
        @Schema(description = "생성자 ID") Long creatorId,
        @Schema(description = "생성자 이름") String creatorName,
        @Schema(description = "조회 시작일") LocalDate dateFrom,
        @Schema(description = "조회 종료일") LocalDate dateTo,
        @Schema(description = "포함된 활동 수") int activityCount,
        @Schema(description = "열람자 수") int viewerCount,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "최종 수정 시각") LocalDateTime updatedAt,
        @Schema(description = "포함된 활동 ID 목록") List<Long> activityIds,
        @Schema(description = "열람자 사용자 ID 목록") List<Long> viewerIds
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
