package com.team2.activity.query.controller;

import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.query.service.ActivityPackagePdfReportService;
import com.team2.activity.query.service.ActivityPackageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/activity-packages")
@RequiredArgsConstructor
public class ActivityPackageQueryController {

    private final ActivityPackageQueryService activityPackageQueryService;
    private final ActivityPackagePdfReportService activityPackagePdfReportService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ActivityPackageResponse>>> getPackages(
            @RequestParam(required = false) Long creatorId) {
        List<ActivityPackage> packages = creatorId != null
                ? activityPackageQueryService.getPackagesByCreatorId(creatorId)
                : activityPackageQueryService.getAllPackages();
        List<EntityModel<ActivityPackageResponse>> models = packages.stream()
                .map(ActivityPackageResponse::from)
                .map(r -> EntityModel.of(r,
                        linkTo(methodOn(ActivityPackageQueryController.class).getPackage(r.packageId())).withSelfRel(),
                        linkTo(methodOn(ActivityPackageQueryController.class).downloadPackageReport(r.packageId(), null)).withRel("report")))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ActivityPackageQueryController.class).getPackages(creatorId)).withSelfRel()));
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<EntityModel<ActivityPackageResponse>> getPackage(@PathVariable Long packageId) {
        ActivityPackage activityPackage = activityPackageQueryService.getPackage(packageId);
        ActivityPackageResponse response = ActivityPackageResponse.from(activityPackage);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(ActivityPackageQueryController.class).getPackage(packageId)).withSelfRel(),
                linkTo(methodOn(ActivityPackageQueryController.class).getPackages(null)).withRel("activity-packages"),
                linkTo(methodOn(ActivityPackageQueryController.class).downloadPackageReport(packageId, null)).withRel("report")));
    }

    @GetMapping(value = "/{packageId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPackageReport(
            @PathVariable Long packageId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ActivityPackage activityPackage = activityPackageQueryService.getPackage(packageId);
        byte[] pdfBytes = activityPackagePdfReportService.generatePackageReport(activityPackage, userId);
        String fileName = activityPackagePdfReportService.getDownloadFileName(activityPackage);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(pdfBytes.length);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .headers(headers)
                .body(pdfBytes);
    }
}
