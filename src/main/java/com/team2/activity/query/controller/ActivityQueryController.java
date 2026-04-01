package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.entity.Activity;
import com.team2.activity.query.service.ActivityQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityQueryController {

    private final ActivityQueryService activityQueryService;

    @GetMapping
    public ResponseEntity<PagedResponse<Activity>> getActivities(
            @RequestParam(required = false) Long clientId) {
        List<Activity> activities = clientId != null
                ? activityQueryService.getActivitiesByClientId(clientId)
                : activityQueryService.getAllActivities();
        return ResponseEntity.ok(PagedResponse.of(activities));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<Activity> getActivity(@PathVariable Long activityId) {
        return ResponseEntity.ok(activityQueryService.getActivity(activityId));
    }
}
