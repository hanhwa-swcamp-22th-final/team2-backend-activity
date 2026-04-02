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

// 활동 패키지 읽기 API를 제공하는 query controller다.
@RestController
// 활동 패키지 조회 엔드포인트 기본 경로를 지정한다.
@RequestMapping("/api/activity-packages")
// 생성자 주입용 생성자를 자동 생성한다.
@RequiredArgsConstructor
public class ActivityPackageQueryController {

    // 활동 패키지 조회 로직을 서비스에 위임한다.
    private final ActivityPackageQueryService activityPackageQueryService;
    // 활동 패키지 PDF 보고서 생성 로직을 서비스에 위임한다.
    private final ActivityPackagePdfReportService activityPackagePdfReportService;

    // 조건에 맞는 패키지 목록을 조회해 페이징 응답 형태로 반환한다.
    @GetMapping
    public ResponseEntity<PagedResponse<ActivityPackageResponse>> getPackages(
            // 필요하면 생성자 ID 조건으로 조회를 제한한다.
            @RequestParam(required = false) Long creatorId) {
        // 파라미터 유무에 따라 전체 조회와 조건 조회를 분기한다.
        List<ActivityPackage> packages = creatorId != null
                // creatorId가 있으면 생성자별 패키지 목록을 조회한다.
                ? activityPackageQueryService.getPackagesByCreatorId(creatorId)
                // creatorId가 없으면 전체 패키지 목록을 조회한다.
                : activityPackageQueryService.getAllPackages();
        // 엔티티 목록을 응답 DTO 목록으로 변환한다.
        List<ActivityPackageResponse> responses = packages.stream()
                // 각 패키지 엔티티를 응답 DTO로 변환한다.
                .map(ActivityPackageResponse::from)
                // 변환된 DTO들을 리스트로 모은다.
                .toList();
        // DTO 목록을 단일 페이지 응답으로 감싸 200 OK로 반환한다.
        return ResponseEntity.ok(PagedResponse.of(responses));
    }

    // 패키지 ID로 단건 상세를 조회한다.
    @GetMapping("/{packageId}")
    public ResponseEntity<ActivityPackageResponse> getPackage(@PathVariable Long packageId) {
        // 서비스에서 엔티티를 읽어 온다.
        ActivityPackage activityPackage = activityPackageQueryService.getPackage(packageId);
        // 응답 DTO로 변환해 반환한다.
        return ResponseEntity.ok(ActivityPackageResponse.from(activityPackage));
    }

    // 패키지 ID를 기준으로 PDF 보고서를 생성해 다운로드 응답으로 반환한다.
    @GetMapping(value = "/{packageId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPackageReport(@PathVariable Long packageId) {
        // 패키지 데이터로 생성한 PDF 바이트 배열을 조회한다.
        byte[] pdfBytes = activityPackagePdfReportService.generatePackageReport(packageId);
        // 다운로드 파일명을 패키지 ID 기반으로 생성한다.
        String fileName = "activity-package-" + packageId + "-report.pdf";
        // 응답 헤더 객체를 생성한다.
        HttpHeaders headers = new HttpHeaders();
        // 브라우저가 첨부파일 다운로드로 처리하도록 Content-Disposition 헤더를 설정한다.
        headers.setContentDisposition(ContentDisposition.attachment()
                // 한글이 포함돼도 깨지지 않도록 UTF-8 기준 파일명을 설정한다.
                .filename(fileName, StandardCharsets.UTF_8)
                // 첨부파일 응답 헤더 구성을 마무리한다.
                .build());
        // 응답 본문 크기를 헤더에 기록한다.
        headers.setContentLength(pdfBytes.length);
        // PDF MIME 타입과 다운로드 헤더를 함께 담아 200 OK로 반환한다.
        return ResponseEntity.ok()
                // 응답 본문의 MIME 타입을 application/pdf로 지정한다.
                .contentType(MediaType.APPLICATION_PDF)
                // 다운로드용 헤더를 함께 설정한다.
                .headers(headers)
                // 생성한 PDF 바이트 배열을 응답 본문으로 반환한다.
                .body(pdfBytes);
    }
}
