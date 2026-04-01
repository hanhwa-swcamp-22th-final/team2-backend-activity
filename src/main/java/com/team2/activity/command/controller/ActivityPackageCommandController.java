package com.team2.activity.command.controller;

import com.team2.activity.command.service.ActivityPackageCommandService;
import com.team2.activity.dto.ActivityPackageCreateRequest;
import com.team2.activity.dto.ActivityPackageResponse;
import com.team2.activity.dto.ActivityPackageUpdateRequest;
import com.team2.activity.entity.ActivityPackage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity-packages")
@RequiredArgsConstructor
public class ActivityPackageCommandController {

    private final ActivityPackageCommandService activityPackageCommandService;

    @PostMapping
    public ResponseEntity<ActivityPackageResponse> createPackage(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivityPackageCreateRequest request) {
        ActivityPackage saved = activityPackageCommandService.createPackage(request.toEntity(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ActivityPackageResponse.from(saved));
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<ActivityPackageResponse> updatePackage(
            @PathVariable Long packageId,
            @RequestBody ActivityPackageUpdateRequest request) {
        ActivityPackage updated = activityPackageCommandService.updateAll(packageId, request);
        return ResponseEntity.ok(ActivityPackageResponse.from(updated));
    }

    @DeleteMapping("/{packageId}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long packageId) {
        activityPackageCommandService.deletePackage(packageId);
        return ResponseEntity.noContent().build();
    }

}
