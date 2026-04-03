package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.query.service.ActivityPackagePdfReportService;
import com.team2.activity.query.service.ActivityPackageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/activity-packages")
@RequiredArgsConstructor
public class ActivityPackageQueryController {

    private final ActivityPackageQueryService activityPackageQueryService;
    private final ActivityPackagePdfReportService activityPackagePdfReportService;

    @GetMapping
    public ResponseEntity<PagedResponse<ActivityPackageResponse>> getPackages(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) String poId) {
        List<ActivityPackageResponse> responses;
        if (userId != null) {
            responses = activityPackageQueryService.getPackagesByViewerUserId(userId, creatorId, poId);
        } else {
            responses = activityPackageQueryService.getPackagesWithFilters(creatorId, poId);
        }
        return ResponseEntity.ok(PagedResponse.of(responses));
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<ActivityPackageResponse> getPackage(@PathVariable Long packageId) {
        ActivityPackage activityPackage = activityPackageQueryService.getPackage(packageId);
        return ResponseEntity.ok(activityPackageQueryService.enrichPackage(activityPackage));
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
