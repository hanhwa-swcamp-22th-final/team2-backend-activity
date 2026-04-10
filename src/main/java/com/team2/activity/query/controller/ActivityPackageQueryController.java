package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.query.service.ActivityPackagePdfReportService;
import com.team2.activity.query.service.ActivityPackageQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "활동 패키지 Query", description = "활동 패키지 조회 및 PDF 리포트 API")
@RestController
@RequestMapping("/api/activity-packages")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
public class ActivityPackageQueryController {

    private final ActivityPackageQueryService activityPackageQueryService;
    private final ActivityPackagePdfReportService activityPackagePdfReportService;

    @Operation(summary = "활동 패키지 목록 조회", description = "필터 조건에 따라 활동 패키지 목록을 페이징 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ActivityPackageResponse>> getPackages(
            @Parameter(description = "요청 사용자 ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "생성자 ID") @RequestParam(name = "creatorId", required = false) Long creatorId,
            @Parameter(description = "PO ID") @RequestParam(name = "poId", required = false) String poId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") int size) {
        List<ActivityPackageResponse> responses = activityPackageQueryService.getPackagesByViewerUserId(userId, creatorId, poId);
        return ResponseEntity.ok(PagedResponse.of(responses, page, size));
    }

    @Operation(summary = "활동 패키지 상세 조회", description = "패키지 ID로 활동 패키지 상세 정보를 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "활동 패키지를 찾을 수 없음")
    })
    @GetMapping("/{packageId}")
    public ResponseEntity<ActivityPackageResponse> getPackage(
            @Parameter(description = "패키지 ID", required = true) @PathVariable("packageId") Long packageId) {
        ActivityPackage activityPackage = activityPackageQueryService.getPackage(packageId);
        return ResponseEntity.ok(activityPackageQueryService.enrichPackage(activityPackage));
    }

    @Operation(summary = "활동 패키지 PDF 리포트 미리보기", description = "활동 패키지를 PDF 리포트로 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF 조회 성공"),
            @ApiResponse(responseCode = "404", description = "활동 패키지를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "PDF 생성 실패")
    })
    @GetMapping(value = "/{packageId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPackageReport(
            @Parameter(description = "패키지 ID", required = true) @PathVariable("packageId") Long packageId,
            @Parameter(description = "요청 사용자 ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        ActivityPackage activityPackage = activityPackageQueryService.getPackage(packageId);
        byte[] pdfBytes = activityPackagePdfReportService.generatePackageReport(activityPackage, userId);
        String fileName = activityPackagePdfReportService.getDownloadFileName(activityPackage);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(pdfBytes.length);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .headers(headers)
                .body(pdfBytes);
    }
}
