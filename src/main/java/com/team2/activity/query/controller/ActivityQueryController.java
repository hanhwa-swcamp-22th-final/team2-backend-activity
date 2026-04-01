package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.query.service.ActivityQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 활동 읽기 API를 제공하는 query controller다.
@RestController
// 활동 조회 엔드포인트 기본 경로를 지정한다.
@RequestMapping("/api/activities")
// 생성자 주입용 생성자를 자동 생성한다.
@RequiredArgsConstructor
public class ActivityQueryController {

    // 활동 조회 로직을 서비스에 위임한다.
    private final ActivityQueryService activityQueryService;

    // 조건에 맞는 활동 목록을 조회해 페이징 응답 형태로 반환한다.
    @GetMapping
    public ResponseEntity<PagedResponse<Activity>> getActivities(
            // 필요하면 거래처 ID 조건으로 조회를 제한한다.
            @RequestParam(required = false) Long clientId) {
        // 파라미터 유무에 따라 전체 조회와 거래처별 조회를 분기한다.
        List<Activity> activities = clientId != null
                // clientId가 있으면 거래처별 활동 목록을 조회한다.
                ? activityQueryService.getActivitiesByClientId(clientId)
                // clientId가 없으면 전체 활동 목록을 조회한다.
                : activityQueryService.getAllActivities();
        // 조회 결과를 단일 페이지 응답으로 감싸 200 OK로 반환한다.
        return ResponseEntity.ok(PagedResponse.of(activities));
    }

    // 활동 ID로 단건 상세를 조회한다.
    @GetMapping("/{activityId}")
    public ResponseEntity<Activity> getActivity(@PathVariable Long activityId) {
        // 서비스에서 조회한 활동 엔티티를 200 OK로 반환한다.
        return ResponseEntity.ok(activityQueryService.getActivity(activityId));
    }
}
