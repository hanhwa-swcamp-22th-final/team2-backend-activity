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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityCommandService.createActivity(request.toEntity(userId)));
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<Activity> updateActivity(
            @PathVariable Long activityId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivityUpdateRequest request) {
        Activity activity = activityCommandService.updateActivity(activityId, request, userId);
        return ResponseEntity.ok(activity);
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) {
        activityCommandService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }
}
