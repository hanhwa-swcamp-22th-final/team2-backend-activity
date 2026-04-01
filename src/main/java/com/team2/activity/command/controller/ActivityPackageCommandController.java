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

// 활동 패키지 쓰기 API를 제공하는 command controller다.
@RestController
// 활동 패키지 관련 command 엔드포인트 기본 경로를 지정한다.
@RequestMapping("/api/activity-packages")
// 생성자 주입용 생성자를 자동 생성한다.
@RequiredArgsConstructor
public class ActivityPackageCommandController {

    // 활동 패키지 쓰기 로직을 서비스에 위임한다.
    private final ActivityPackageCommandService activityPackageCommandService;

    // 새 활동 패키지를 생성하고 응답 DTO로 반환한다.
    @PostMapping
    public ResponseEntity<ActivityPackageResponse> createPackage(
            // 헤더에서 현재 사용자 ID를 생성자로 받는다.
            @RequestHeader("X-User-Id") Long userId,
            // 요청 본문을 검증한 뒤 DTO로 바인딩한다.
            @Valid @RequestBody ActivityPackageCreateRequest request) {
        // DTO를 엔티티로 바꿔 서비스에 저장을 위임한다.
        ActivityPackage saved = activityPackageCommandService.createPackage(request.toEntity(userId));
        // 응답 상태 코드를 201 Created로 설정한다.
        return ResponseEntity.status(HttpStatus.CREATED)
                // 저장된 엔티티를 응답 DTO로 변환한다.
                .body(ActivityPackageResponse.from(saved));
    }

    // 패키지 기본 정보와 viewer/item 구성을 한 번에 수정한다.
    @PutMapping("/{packageId}")
    public ResponseEntity<ActivityPackageResponse> updatePackage(
            // URL 경로에서 수정 대상 패키지 ID를 받는다.
            @PathVariable Long packageId,
            // 요청 본문을 수정 DTO로 바인딩한다.
            @RequestBody ActivityPackageUpdateRequest request) {
        // 서비스 계층에서 수정된 엔티티를 받아 온다.
        ActivityPackage updated = activityPackageCommandService.updateAll(packageId, request);
        // 수정된 엔티티를 응답 DTO로 바꿔 200 OK로 반환한다.
        return ResponseEntity.ok(ActivityPackageResponse.from(updated));
    }

    // 패키지 삭제 요청을 받아 대상 패키지를 제거한다.
    @DeleteMapping("/{packageId}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long packageId) {
        // 삭제 처리는 서비스 계층에 위임한다.
        activityPackageCommandService.deletePackage(packageId);
        // 응답 본문 없이 204 No Content를 반환한다.
        return ResponseEntity.noContent().build();
    }
}
