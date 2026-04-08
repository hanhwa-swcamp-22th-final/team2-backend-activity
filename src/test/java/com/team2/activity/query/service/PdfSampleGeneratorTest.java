package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.query.controller.ActivityPackageQueryController;
import com.team2.activity.query.dto.ActivityResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ContentDisposition;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Spring 컨텍스트 없이 실제 PDF를 생성하고 HTTP 응답에 포함되는지 검증한다.
@ExtendWith(MockitoExtension.class)
class PdfSampleGeneratorTest {

    @Mock
    private ActivityQueryService activityQueryService;

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private DocumentsFeignClient documentsFeignClient;

    @Mock
    private ActivityPackageQueryService activityPackageQueryService;

    @InjectMocks
    private ActivityPackagePdfReportService pdfReportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 실제 PDF 서비스와 컨트롤러를 연결해 MockMvc를 구성한다.
        ActivityPackageQueryController controller =
                new ActivityPackageQueryController(activityPackageQueryService, pdfReportService);
        // @AuthenticationPrincipal Jwt 인자를 standalone setup에서도 해석할 수 있도록 resolver 등록.
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        // SecurityContext에 테스트용 JwtAuthenticationToken 삽입 — @AuthenticationPrincipal이 이를 꺼내 쓴다.
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("7")
                .claim("role", "ADMIN")
                .claim("name", "test-admin")
                .claim("email", "test-admin@team2.local")
                .claim("departmentId", 1)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/activity-packages/1/report → 200 OK, PDF 바이트가 응답 본문에 포함된다")
    void downloadPackageReport_returnsPdfInResponse() throws Exception {
        // ── 패키지 mock 설정 ─────────────────────────────────────────
        ActivityPackage pkg = mock(ActivityPackage.class);
        when(pkg.getPackageTitle()).thenReturn("2025년 3월 영업활동 보고서");

        // ── 패키지 아이템 4개 (MEETING / ISSUE / SCHEDULE / MEMO) ────
        ActivityPackageItem item1 = mock(ActivityPackageItem.class);
        when(item1.getActivityId()).thenReturn(1L);

        ActivityPackageItem item2 = mock(ActivityPackageItem.class);
        when(item2.getActivityId()).thenReturn(2L);

        ActivityPackageItem item3 = mock(ActivityPackageItem.class);
        when(item3.getActivityId()).thenReturn(3L);

        ActivityPackageItem item4 = mock(ActivityPackageItem.class);
        when(item4.getActivityId()).thenReturn(4L);

        when(pkg.getItems()).thenReturn(List.of(item1, item2, item3, item4));

        // ── 활동 데이터 mock 설정 ────────────────────────────────────
        // 1. MEETING
        when(activityQueryService.getActivity(1L)).thenReturn(new ActivityResponse(
                1L, 10L, "PO-2025-001", 7L,
                LocalDate.of(2025, 3, 5),
                ActivityType.MEETING,
                "1분기 영업 전략 미팅",
                "거래처 A사 방문, 신규 계약 조건 협의 및 샘플 전달",
                Priority.HIGH,
                null, null, null, null,
                "김영업", "A무역"
        ));

        // 2. ISSUE
        when(activityQueryService.getActivity(2L)).thenReturn(new ActivityResponse(
                2L, 10L, "PO-2025-001", 7L,
                LocalDate.of(2025, 3, 10),
                ActivityType.ISSUE,
                "불량률 이슈 접수",
                "A사에서 전달받은 샘플 중 3% 불량 발생. 품질팀 검토 요청",
                Priority.HIGH,
                null, null, null, null,
                "김영업", "A무역"
        ));

        // 3. SCHEDULE (시작일·종료일 있음)
        when(activityQueryService.getActivity(3L)).thenReturn(new ActivityResponse(
                3L, 10L, "PO-2025-001", 7L,
                LocalDate.of(2025, 3, 12),
                ActivityType.SCHEDULE,
                "제품 납기 관리 일정",
                "생산 라인 점검 및 선적 일정 조율",
                Priority.NORMAL,
                LocalDate.of(2025, 3, 12),
                LocalDate.of(2025, 3, 25),
                null, null,
                "김영업", "A무역"
        ));

        // 4. MEMO
        when(activityQueryService.getActivity(4L)).thenReturn(new ActivityResponse(
                4L, 10L, null, 7L,
                LocalDate.of(2025, 3, 20),
                ActivityType.MEMO,
                "클레임 대응 내용 메모",
                "불량 원인 분석 완료. 다음 로트부터 공정 개선 적용 예정",
                null,
                null, null, null, null,
                "이담당", "A무역"
        ));

        // ── 작성자 이름 mock 설정 ────────────────────────────────────
        UserResponse creator = mock(UserResponse.class);
        when(creator.getName()).thenReturn("박과장");
        when(authFeignClient.getUser(7L)).thenReturn(creator);

        // ── 패키지 조회 mock — 컨트롤러가 서비스에서 패키지를 가져오도록 연결한다 ──
        when(activityPackageQueryService.getPackage(1L)).thenReturn(pkg);

        // ── HTTP GET 요청 → PDF가 응답 본문에 포함되는지 검증한다 ─────
        MvcResult result = mockMvc.perform(get("/api/activity-packages/1/report")
                        .header("X-User-Id", "7"))
                // 응답 상태가 200 OK인지 확인한다.
                .andExpect(status().isOk())
                // 응답 MIME 타입이 application/pdf인지 확인한다.
                .andExpect(content().contentType("application/pdf"))
                // Content-Disposition이 inline 형태인지 확인한다.
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("inline")))
                .andReturn();

        // ── 응답 본문이 유효한 PDF인지 시그니처(%PDF)로 확인한다 ────────
        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        assertThat(new String(body, 0, 4)).isEqualTo("%PDF");

        // ── Content-Disposition 파일명이 패키지 제목 기반인지 확인한다 ──
        String contentDisposition = result.getResponse().getHeader("Content-Disposition");
        String filename = ContentDisposition.parse(contentDisposition).getFilename();
        assertThat(filename).isEqualTo("2025년 3월 영업활동 보고서.pdf");
    }
}
