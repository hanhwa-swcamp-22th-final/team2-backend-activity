package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.query.dto.ActivityResponse;
import com.team2.activity.query.service.ActivityQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityQueryController {

    private final ActivityQueryService activityQueryService;

    @GetMapping
    public ResponseEntity<PagedResponse<ActivityResponse>> getActivities(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String poId,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) Long activityAuthorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activityDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activityDateTo,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ActivityResponse> responses = activityQueryService.getActivitiesWithFilters(
                clientId, poId, activityType, activityAuthorId, activityDateFrom, activityDateTo, keyword, page, size);
        long totalElements = activityQueryService.countWithFilters(
                clientId, poId, activityType, activityAuthorId, activityDateFrom, activityDateTo, keyword);
        return ResponseEntity.ok(PagedResponse.of(responses, totalElements, page, size));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable Long activityId) {
        return ResponseEntity.ok(activityQueryService.getActivity(activityId));
    }
}
