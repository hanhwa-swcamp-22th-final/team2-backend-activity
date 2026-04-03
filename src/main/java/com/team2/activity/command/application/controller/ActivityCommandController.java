package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ActivityCommandService;
import com.team2.activity.command.application.dto.ActivityCreateRequest;
import com.team2.activity.command.application.dto.ActivityUpdateRequest;
import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.query.controller.ActivityQueryController;
import com.team2.activity.query.dto.ActivityResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityCommandController {

    private final ActivityCommandService activityCommandService;

    @PostMapping
    public ResponseEntity<EntityModel<ActivityResponse>> createActivity(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivityCreateRequest request) {
        Activity activity = activityCommandService.createActivity(request.toEntity(userId));
        EntityModel<ActivityResponse> model = EntityModel.of(ActivityResponse.from(activity),
                linkTo(methodOn(ActivityQueryController.class).getActivity(activity.getActivityId())).withSelfRel(),
                linkTo(methodOn(ActivityQueryController.class).getActivities(null)).withRel("activities"));
        URI location = linkTo(methodOn(ActivityQueryController.class).getActivity(activity.getActivityId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<EntityModel<ActivityResponse>> updateActivity(
            @PathVariable Long activityId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivityUpdateRequest request) {
        Activity activity = activityCommandService.updateActivity(activityId, request, userId);
        return ResponseEntity.ok(EntityModel.of(ActivityResponse.from(activity),
                linkTo(methodOn(ActivityQueryController.class).getActivity(activityId)).withSelfRel(),
                linkTo(methodOn(ActivityQueryController.class).getActivities(null)).withRel("activities")));
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) {
        activityCommandService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }
}
