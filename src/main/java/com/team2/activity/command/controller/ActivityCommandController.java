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

// 활동 쓰기 API를 노출하는 command controller다.
@RestController
// 활동 관련 command 엔드포인트 기본 경로를 지정한다.
@RequestMapping("/api/activities")
// 생성자 주입용 생성자를 Lombok으로 생성한다.
@RequiredArgsConstructor
public class ActivityCommandController {

    // 활동 쓰기 로직을 서비스 계층에 위임한다.
    private final ActivityCommandService activityCommandService;

    // 활동 생성 요청을 받아 저장한 뒤 생성된 엔티티를 반환한다.
    @PostMapping
    public ResponseEntity<Activity> createActivity(
            // 헤더에서 현재 사용자 ID를 받아 작성자로 사용한다.
            @RequestHeader("X-User-Id") Long userId,
            // 요청 본문을 검증한 뒤 DTO로 바인딩한다.
            @Valid @RequestBody ActivityCreateRequest request) {
        // 응답 상태 코드를 201 Created로 설정한다.
        return ResponseEntity.status(HttpStatus.CREATED)
                // DTO를 엔티티로 바꿔 서비스에 전달한다.
                .body(activityCommandService.createActivity(request.toEntity(userId)));
    }

    // 활동 수정 요청을 받아 기존 활동을 갱신한다.
    @PutMapping("/{activityId}")
    public ResponseEntity<Activity> updateActivity(
            // URL 경로에서 수정 대상 활동 ID를 받는다.
            @PathVariable Long activityId,
            // 헤더에서 수정 수행 사용자 ID를 받는다.
            @RequestHeader("X-User-Id") Long userId,
            // 요청 본문을 검증한 뒤 DTO로 바인딩한다.
            @Valid @RequestBody ActivityUpdateRequest request) {
        // 수정된 엔티티를 서비스에서 받아 응답으로 반환한다.
        Activity activity = activityCommandService.updateActivity(activityId, request, userId);
        // 수정 성공 응답 본문으로 갱신된 엔티티를 반환한다.
        return ResponseEntity.ok(activity);
    }

    // 활동 삭제 요청을 받아 대상 활동을 제거한다.
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) {
        // 삭제 처리는 서비스 계층에 위임한다.
        activityCommandService.deleteActivity(activityId);
        // 응답 본문 없이 204 No Content를 반환한다.
        return ResponseEntity.noContent().build();
    }
}
