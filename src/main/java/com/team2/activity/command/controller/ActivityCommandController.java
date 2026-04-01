package com.team2.activity.command.controller;

import com.team2.activity.command.service.ActivityCommandService;
import com.team2.activity.dto.ActivityCreateRequest;
import com.team2.activity.dto.ActivityUpdateRequest;
import com.team2.activity.entity.Activity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityCommandController {

    private final ActivityCommandService activityCommandService;

    @PostMapping
    public ResponseEntity<Activity> createActivity(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivityCreateRequest request) {
        Activity activity = Activity.builder()
                .clientId(request.clientId())
                .activityDate(request.activityDate())
                .activityType(request.activityType())
                .activityTitle(request.activityTitle())
                .activityAuthorId(userId)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityCommandService.createActivity(activity));
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<Activity> updateActivity(
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityUpdateRequest request) {
        Activity activity = activityCommandService.updateActivity(
                activityId,
                request.activityType(),
                request.activityTitle(),
                request.activityContent(),
                request.activityDate(),
                null,
                request.poId(),
                request.activityPriority(),
                request.activityScheduleFrom(),
                request.activityScheduleTo());
        return ResponseEntity.ok(activity);
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) {
        activityCommandService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }
}
