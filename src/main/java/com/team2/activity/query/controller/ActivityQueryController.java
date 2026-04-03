package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.query.dto.ActivityResponse;
import com.team2.activity.query.service.ActivityQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityQueryController {

    private final ActivityQueryService activityQueryService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ActivityResponse>>> getActivities(
            @RequestParam(required = false) Long clientId) {
        List<ActivityResponse> activities = (clientId != null
                ? activityQueryService.getActivitiesByClientId(clientId)
                : activityQueryService.getAllActivities())
                .stream().map(ActivityResponse::from).toList();

        List<EntityModel<ActivityResponse>> models = activities.stream()
                .map(a -> EntityModel.of(a,
                        linkTo(methodOn(ActivityQueryController.class).getActivity(a.activityId())).withSelfRel()))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ActivityQueryController.class).getActivities(clientId)).withSelfRel()));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<EntityModel<ActivityResponse>> getActivity(@PathVariable Long activityId) {
        ActivityResponse response = activityQueryService.getActivity(activityId);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(ActivityQueryController.class).getActivity(activityId)).withSelfRel(),
                linkTo(methodOn(ActivityQueryController.class).getActivities(null)).withRel("activities")));
    }
}
