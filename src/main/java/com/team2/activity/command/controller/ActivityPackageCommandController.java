package com.team2.activity.command.controller;

import com.team2.activity.command.service.ActivityPackageCommandService;
import com.team2.activity.dto.ActivityPackageCreateRequest;
import com.team2.activity.dto.ActivityPackageResponse;
import com.team2.activity.dto.ActivityPackageUpdateRequest;
import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.entity.ActivityPackageItem;
import com.team2.activity.entity.ActivityPackageViewer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-packages")
@RequiredArgsConstructor
public class ActivityPackageCommandController {

    private final ActivityPackageCommandService activityPackageCommandService;

    @PostMapping
    public ResponseEntity<ActivityPackageResponse> createPackage(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivityPackageCreateRequest request) {
        List<ActivityPackageViewer> viewers = toViewers(request.viewerIds());
        List<ActivityPackageItem> items = toItems(request.activityIds());
        ActivityPackage activityPackage = ActivityPackage.builder()
                .packageTitle(request.packageTitle())
                .packageDescription(request.packageDescription())
                .poId(request.poId())
                .creatorId(userId)
                .viewers(viewers)
                .items(items)
                .build();
        ActivityPackage saved = activityPackageCommandService.createPackage(activityPackage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ActivityPackageResponse.from(saved));
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<ActivityPackageResponse> updatePackage(
            @PathVariable Long packageId,
            @RequestBody ActivityPackageUpdateRequest request) {
        activityPackageCommandService.updatePackage(
                packageId,
                request.packageTitle(),
                request.packageDescription(),
                request.poId());
        if (request.viewerIds() != null) {
            activityPackageCommandService.updateViewers(packageId, request.viewerIds());
        }
        ActivityPackage updated = activityPackageCommandService.updateItems(
                packageId, request.activityIds() != null ? request.activityIds() : List.of());
        return ResponseEntity.ok(ActivityPackageResponse.from(updated));
    }

    @DeleteMapping("/{packageId}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long packageId) {
        activityPackageCommandService.deletePackage(packageId);
        return ResponseEntity.noContent().build();
    }

    private List<ActivityPackageViewer> toViewers(List<Long> ids) {
        if (ids == null) return List.of();
        return ids.stream().map(ActivityPackageViewer::of).toList();
    }

    private List<ActivityPackageItem> toItems(List<Long> ids) {
        if (ids == null) return List.of();
        return ids.stream().map(ActivityPackageItem::of).toList();
    }
}
