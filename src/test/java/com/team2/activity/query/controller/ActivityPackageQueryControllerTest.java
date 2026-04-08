package com.team2.activity.query.controller;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.query.service.ActivityPackagePdfReportService;
import com.team2.activity.query.service.ActivityPackageQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.ContentDisposition;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ActivityPackage 조회 API의 응답 구조와 예외 변환을 검증한다.
@WebMvcTest(ActivityPackageQueryController.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("ActivityPackageQueryController 테스트")
class ActivityPackageQueryControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void initMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(get("/").with(jwt().jwt(j -> j
                        .subject("10")
                        .claim("role", "ADMIN")
                        .claim("name", "test-admin")
                        .claim("email", "test-admin@team2.local")
                        .claim("departmentId", 1))))
                .build();
    }

    // 컨트롤러가 호출할 패키지 조회 서비스 목 객체다.
    @MockitoBean
    private ActivityPackageQueryService activityPackageQueryService;

    // 컨트롤러가 호출할 PDF 보고서 생성 서비스 목 객체다.
    @MockitoBean
    private ActivityPackagePdfReportService activityPackagePdfReportService;

    // 응답 검증에 사용할 공통 ActivityPackage 픽스처를 만든다.
    private ActivityPackage buildPackage() {
        return ActivityPackage.builder()
                // 테스트용 패키지 ID를 설정한다.
                .packageId(1L)
                // 테스트용 패키지 제목을 설정한다.
                .packageTitle("주간 패키지")
                // 테스트용 생성자 ID를 설정한다.
                .creatorId(10L)
                // 공통 ActivityPackage 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("GET /api/activity-packages → 200 OK, PagedResponse 구조로 목록 반환")
    void getPackages_returns200WithPagedResult() throws Exception {
        ActivityPackage pkg = buildPackage();
        when(activityPackageQueryService.getPackagesWithFilters(isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(List.of(ActivityPackageResponse.from(pkg)));
        when(activityPackageQueryService.countPackagesWithFilters(isNull(), isNull()))
                .thenReturn(1L);

        mockMvc.perform(get("/api/activity-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/activity-packages?creator_id=10 → 200 OK, creator_id 필터 적용")
    void getPackages_returns200WithCreatorIdFilter() throws Exception {
        ActivityPackage pkg = buildPackage();
        when(activityPackageQueryService.getPackagesWithFilters(any(), isNull(), anyInt(), anyInt()))
                .thenReturn(List.of(ActivityPackageResponse.from(pkg)));
        when(activityPackageQueryService.countPackagesWithFilters(any(), isNull()))
                .thenReturn(1L);

        mockMvc.perform(get("/api/activity-packages").param("creatorId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/activity-packages/{package_id} → 200 OK, 상세 필드 포함")
    void getPackage_returns200WithDetail() throws Exception {
        ActivityPackage pkg = buildPackage();
        when(activityPackageQueryService.getPackage(1L)).thenReturn(pkg);
        when(activityPackageQueryService.enrichPackage(any(ActivityPackage.class)))
                .thenReturn(ActivityPackageResponse.from(pkg));

        mockMvc.perform(get("/api/activity-packages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.package_id").exists())
                .andExpect(jsonPath("$.package_title").exists())
                .andExpect(jsonPath("$.activity_ids").isArray())
                .andExpect(jsonPath("$.viewer_ids").isArray());
    }

    @Test
    @DisplayName("GET /api/activity-packages/{package_id} - 존재하지 않는 ID → 404 Not Found")
    void getPackage_returns404WhenNotFound() throws Exception {
        // 서비스 조회 실패 예외가 404로 매핑되는지 검증한다.
        when(activityPackageQueryService.getPackage(999L))
                .thenThrow(new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."));

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(get("/api/activity-packages/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/activity-packages/{package_id}/report → 200 OK, PDF inline 응답")
    void downloadPackageReport_returnsPdfAttachment() throws Exception {
        // PDF 다운로드 응답 본문으로 사용할 바이트 배열을 준비한다.
        byte[] pdfBytes = "%PDF-1.7".getBytes();
        // 다운로드 파일명 생성에 사용할 패키지 엔티티를 준비한다.
        ActivityPackage activityPackage = buildPackage();
        // 컨트롤러가 기존 PKG 조회 로직으로 패키지 엔티티를 읽도록 설정한다.
        when(activityPackageQueryService.getPackage(1L)).thenReturn(activityPackage);
        // 컨트롤러가 PDF 생성 서비스 결과를 그대로 사용하도록 설정한다 (userId 파라미터 포함).
        when(activityPackagePdfReportService.generatePackageReport(any(ActivityPackage.class), any())).thenReturn(pdfBytes);
        // 컨트롤러가 PDF 파일명을 패키지 제목 기준으로 생성하도록 설정한다.
        when(activityPackagePdfReportService.getDownloadFileName(any(ActivityPackage.class))).thenReturn("주간 패키지.pdf");

        // PDF 다운로드 요청이 정상 응답과 첨부파일 헤더를 반환하는지 확인한다.
        String contentDisposition = mockMvc.perform(get("/api/activity-packages/1/report")
                        // 헤더에 요청자 ID를 실어 보낸다.
                        .header("X-User-Id", "10"))
                // 응답 상태가 200 OK인지 확인한다.
                .andExpect(status().isOk())
                // 응답 MIME 타입이 application/pdf인지 확인한다.
                .andExpect(content().contentType("application/pdf"))
                // Content-Disposition이 inline 형태인지 확인한다 (브라우저에서 바로 열어 인쇄 가능).
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
                // 응답 본문이 PDF 바이트 배열과 같은지 확인한다.
                .andExpect(content().bytes(pdfBytes))
                // 응답 헤더 값을 후속 검증을 위해 추출한다.
                .andReturn()
                // Content-Disposition 헤더 문자열을 꺼낸다.
                .getResponse()
                // Content-Disposition 헤더 값을 반환한다.
                .getHeader("Content-Disposition");
        // 다운로드 헤더 문자열을 ContentDisposition 객체로 파싱한다.
        ContentDisposition parsedContentDisposition = ContentDisposition.parse(contentDisposition);
        // 파싱된 파일명이 패키지 제목 기반 PDF 파일명인지 확인한다.
        assertThat(parsedContentDisposition.getFilename()).isEqualTo("주간 패키지.pdf");
    }
}
