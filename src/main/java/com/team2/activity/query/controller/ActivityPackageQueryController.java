package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.dto.ActivityPackageResponse;
import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.query.service.ActivityPackageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-packages")
@RequiredArgsConstructor
public class ActivityPackageQueryController {

    private final ActivityPackageQueryService activityPackageQueryService;

    @GetMapping
    public ResponseEntity<PagedResponse<ActivityPackageResponse>> getPackages(
            @RequestParam(required = false) Long creatorId) {
        List<ActivityPackage> packages = creatorId != null
                ? activityPackageQueryService.getPackagesByCreatorId(creatorId)
                : activityPackageQueryService.getAllPackages();
        List<ActivityPackageResponse> responses = packages.stream()
                .map(ActivityPackageResponse::from)
                .toList();
        return ResponseEntity.ok(PagedResponse.of(responses));
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<ActivityPackageResponse> getPackage(@PathVariable Long packageId) {
        ActivityPackage activityPackage = activityPackageQueryService.getPackage(packageId);
        return ResponseEntity.ok(ActivityPackageResponse.from(activityPackage));
    }
}
