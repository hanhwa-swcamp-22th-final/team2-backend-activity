package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ActivityPackageCommandService;
import com.team2.activity.command.application.dto.ActivityPackageCreateRequest;
import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.command.application.dto.ActivityPackageUpdateRequest;
import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.query.controller.ActivityPackageQueryController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "활동 패키지 Command", description = "활동 패키지 생성/수정/삭제 API")
@RestController
@RequestMapping("/api/activity-packages")
@RequiredArgsConstructor
public class ActivityPackageCommandController {

    private final ActivityPackageCommandService activityPackageCommandService;

    @Operation(summary = "활동 패키지 생성", description = "새로운 활동 패키지를 생성한다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "활동 패키지 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    public ResponseEntity<EntityModel<ActivityPackageResponse>> createPackage(
            @Parameter(description = "요청 사용자 ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivityPackageCreateRequest request) {
        ActivityPackage saved = activityPackageCommandService.createPackage(request.toEntity(userId));
        EntityModel<ActivityPackageResponse> model = EntityModel.of(ActivityPackageResponse.from(saved),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackage(saved.getPackageId())).withSelfRel(),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackages(null, null, null, 0, 20)).withRel("activity-packages"));
        URI location = linkTo(methodOn(ActivityPackageQueryController.class).getPackage(saved.getPackageId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "활동 패키지 수정", description = "기존 활동 패키지 정보를 수정한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "활동 패키지 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "활동 패키지를 찾을 수 없음")
    })
    @PutMapping("/{packageId}")
    public ResponseEntity<EntityModel<ActivityPackageResponse>> updatePackage(
            @Parameter(description = "패키지 ID", required = true) @PathVariable Long packageId,
            @Valid @RequestBody ActivityPackageUpdateRequest request) {
        ActivityPackage updated = activityPackageCommandService.updateAll(packageId, request);
        return ResponseEntity.ok(EntityModel.of(ActivityPackageResponse.from(updated),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackage(packageId)).withSelfRel(),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackages(null, null, null, 0, 20)).withRel("activity-packages")));
    }

    @Operation(summary = "활동 패키지 삭제", description = "활동 패키지를 삭제한다")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "활동 패키지 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "활동 패키지를 찾을 수 없음")
    })
    @DeleteMapping("/{packageId}")
    public ResponseEntity<Void> deletePackage(
            @Parameter(description = "패키지 ID", required = true) @PathVariable Long packageId) {
        activityPackageCommandService.deletePackage(packageId);
        return ResponseEntity.noContent().build();
    }
}
