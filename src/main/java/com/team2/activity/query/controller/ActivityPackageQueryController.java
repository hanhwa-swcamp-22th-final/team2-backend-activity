package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.query.service.ActivityPackageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 활동 패키지 읽기 API를 제공하는 query controller다.
@RestController
// 활동 패키지 조회 엔드포인트 기본 경로를 지정한다.
@RequestMapping("/api/activity-packages")
// 생성자 주입용 생성자를 자동 생성한다.
@RequiredArgsConstructor
public class ActivityPackageQueryController {

    // 활동 패키지 조회 로직을 서비스에 위임한다.
    private final ActivityPackageQueryService activityPackageQueryService;

    // 조건에 맞는 패키지 목록을 조회해 페이징 응답 형태로 반환한다.
    @GetMapping
    public ResponseEntity<PagedResponse<ActivityPackageResponse>> getPackages(
            // 필요하면 생성자 ID 조건으로 조회를 제한한다.
            @RequestParam(required = false) Long creatorId) {
        // 파라미터 유무에 따라 전체 조회와 조건 조회를 분기한다.
        List<ActivityPackage> packages = creatorId != null
                // creatorId가 있으면 생성자별 패키지 목록을 조회한다.
                ? activityPackageQueryService.getPackagesByCreatorId(creatorId)
                // creatorId가 없으면 전체 패키지 목록을 조회한다.
                : activityPackageQueryService.getAllPackages();
        // 엔티티 목록을 응답 DTO 목록으로 변환한다.
        List<ActivityPackageResponse> responses = packages.stream()
                // 각 패키지 엔티티를 응답 DTO로 변환한다.
                .map(ActivityPackageResponse::from)
                // 변환된 DTO들을 리스트로 모은다.
                .toList();
        // DTO 목록을 단일 페이지 응답으로 감싸 200 OK로 반환한다.
        return ResponseEntity.ok(PagedResponse.of(responses));
    }

    // 패키지 ID로 단건 상세를 조회한다.
    @GetMapping("/{packageId}")
    public ResponseEntity<ActivityPackageResponse> getPackage(@PathVariable Long packageId) {
        // 서비스에서 엔티티를 읽어 온다.
        ActivityPackage activityPackage = activityPackageQueryService.getPackage(packageId);
        // 응답 DTO로 변환해 반환한다.
        return ResponseEntity.ok(ActivityPackageResponse.from(activityPackage));
    }
}
