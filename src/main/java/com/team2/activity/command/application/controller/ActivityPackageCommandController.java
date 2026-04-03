package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ActivityPackageCommandService;
import com.team2.activity.command.application.dto.ActivityPackageCreateRequest;
import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.command.application.dto.ActivityPackageUpdateRequest;
import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.query.controller.ActivityPackageQueryController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/activity-packages")
@RequiredArgsConstructor
public class ActivityPackageCommandController {

    private final ActivityPackageCommandService activityPackageCommandService;

    @PostMapping
    public ResponseEntity<EntityModel<ActivityPackageResponse>> createPackage(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivityPackageCreateRequest request) {
        ActivityPackage saved = activityPackageCommandService.createPackage(request.toEntity(userId));
        EntityModel<ActivityPackageResponse> model = EntityModel.of(ActivityPackageResponse.from(saved),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackage(saved.getPackageId())).withSelfRel(),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackages(null)).withRel("activity-packages"));
        URI location = linkTo(methodOn(ActivityPackageQueryController.class).getPackage(saved.getPackageId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<EntityModel<ActivityPackageResponse>> updatePackage(
            @PathVariable Long packageId,
            @Valid @RequestBody ActivityPackageUpdateRequest request) {
        ActivityPackage updated = activityPackageCommandService.updateAll(packageId, request);
        return ResponseEntity.ok(EntityModel.of(ActivityPackageResponse.from(updated),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackage(packageId)).withSelfRel(),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackages(null)).withRel("activity-packages")));
    }

    @DeleteMapping("/{packageId}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long packageId) {
        activityPackageCommandService.deletePackage(packageId);
        return ResponseEntity.noContent().build();
    }
}
