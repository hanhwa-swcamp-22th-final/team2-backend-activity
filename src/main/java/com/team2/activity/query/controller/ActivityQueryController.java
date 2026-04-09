package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.query.dto.ActivityResponse;
import com.team2.activity.query.service.ActivityQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "활동기록 Query", description = "활동기록 조회 API")
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ActivityQueryController {

    private final ActivityQueryService activityQueryService;

    @Operation(summary = "활동기록 목록 조회", description = "필터 조건에 따라 활동 목록을 페이징 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ActivityResponse>> getActivities(
            @Parameter(description = "거래처 ID") @RequestParam(required = false) Long clientId,
            @Parameter(description = "PO ID") @RequestParam(required = false) String poId,
            @Parameter(description = "활동 유형") @RequestParam(required = false) ActivityType activityType,
            @Parameter(description = "작성자 ID") @RequestParam(required = false) Long activityAuthorId,
            @Parameter(description = "활동 시작일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activityDateFrom,
            @Parameter(description = "활동 종료일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activityDateTo,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {
        List<ActivityResponse> activities = activityQueryService.getActivitiesWithFilters(
                clientId, poId, activityType, activityAuthorId, activityDateFrom, activityDateTo, keyword, page, size);
        long totalElements = activityQueryService.countWithFilters(
                clientId, poId, activityType, activityAuthorId, activityDateFrom, activityDateTo, keyword);
        return ResponseEntity.ok(PagedResponse.of(activities, totalElements, page, size));
    }

    @Operation(summary = "활동 상세 조회", description = "활동 ID로 활동 상세 정보를 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
    })
    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(
            @Parameter(description = "활동 ID", required = true) @PathVariable Long activityId) {
        return ResponseEntity.ok(activityQueryService.getActivity(activityId));
    }
}
