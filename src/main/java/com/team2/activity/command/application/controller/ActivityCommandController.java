package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ActivityCommandService;
import com.team2.activity.command.application.dto.ActivityCreateRequest;
import com.team2.activity.command.application.dto.ActivityUpdateRequest;
import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.query.controller.ActivityQueryController;
import com.team2.activity.query.dto.ActivityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "활동기록 Command", description = "활동기록 생성/수정/삭제 API")
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
public class ActivityCommandController {

    private final ActivityCommandService activityCommandService;

    @Operation(summary = "활동기록 생성", description = "새로운 활동기록을 생성한다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "활동기록 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    public ResponseEntity<EntityModel<ActivityResponse>> createActivity(
            @Parameter(description = "요청 사용자 ID", required = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ActivityCreateRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        Activity activity = activityCommandService.createActivity(request.toEntity(userId));
        EntityModel<ActivityResponse> model = EntityModel.of(ActivityResponse.from(activity),
                linkTo(methodOn(ActivityQueryController.class).getActivity(activity.getActivityId())).withSelfRel(),
                linkTo(methodOn(ActivityQueryController.class).getActivities(null, null, null, null, null, null, null, 0, 20)).withRel("activities"));
        URI location = linkTo(methodOn(ActivityQueryController.class).getActivity(activity.getActivityId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "활동기록 수정", description = "기존 활동기록 정보를 수정한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "활동기록 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "활동기록을 찾을 수 없음")
    })
    @PutMapping("/{activityId}")
    public ResponseEntity<EntityModel<ActivityResponse>> updateActivity(
            @Parameter(description = "활동기록 ID", required = true) @PathVariable Long activityId,
            @Parameter(description = "요청 사용자 ID", required = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ActivityUpdateRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        Activity activity = activityCommandService.updateActivity(activityId, request, userId);
        return ResponseEntity.ok(EntityModel.of(ActivityResponse.from(activity),
                linkTo(methodOn(ActivityQueryController.class).getActivity(activityId)).withSelfRel(),
                linkTo(methodOn(ActivityQueryController.class).getActivities(null, null, null, null, null, null, null, 0, 20)).withRel("activities")));
    }

    @Operation(summary = "활동기록 삭제", description = "활동기록을 삭제한다")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "활동기록 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "활동기록을 찾을 수 없음")
    })
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @Parameter(description = "활동기록 ID", required = true) @PathVariable Long activityId) {
        activityCommandService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }
}
