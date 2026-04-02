package com.team2.activity.integration;

import com.team2.activity.command.infrastructure.client.PurchaseOrderResponse;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.command.domain.repository.ActivityPackageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ActivityPackage API의 생성, 조회, 수정, 삭제가 전체 계층에서 동작하는지 검증한다.
@DisplayName("ActivityPackage 통합 테스트")
class ActivityPackageIntegrationTest extends IntegrationTestSupport {

    // 삭제 이후 DB 상태를 직접 확인할 repository다.
    @Autowired
    private ActivityPackageRepository activityPackageRepository;

    @Test
    @DisplayName("패키지 생성 후 조회, 수정, 삭제까지 통합 흐름을 검증한다")
    void activityPackageCrudFlow() throws Exception {
        // 패키지 생성 요청을 보내고 생성된 package_id를 응답에서 추출한다.
        MvcResult createResult = mockMvc.perform(post("/api/activity-packages")
                        .with(csrf())
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "package_title": "주간 패키지",
                                  "package_description": "주간 활동 묶음",
                                  "po_id": "PO-001",
                                  "activity_ids": [100, 101],
                                  "viewer_ids": [2, 3]
                                }
                                """))
                .andExpect(status().isCreated())
                // 생성 응답에 package_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.package_id").exists())
                // 생성 응답에 creator_id가 헤더 값으로 반영됐는지 확인한다.
                .andExpect(jsonPath("$.creator_id").value(7))
                // 생성 응답의 첫 activity_id가 요청 값과 같은지 확인한다.
                .andExpect(jsonPath("$.activity_ids[0]").value(100))
                // 생성 응답의 첫 viewer_id가 요청 값과 같은지 확인한다.
                .andExpect(jsonPath("$.viewer_ids[0]").value(2))
                .andReturn();

        // 후속 요청에 사용할 package_id를 응답 본문에서 읽어 온다.
        long packageId = extractLong(createResult, "package_id");

        // 생성된 패키지가 상세 조회 API에서 조회되는지 확인한다.
        mockMvc.perform(get("/api/activity-packages/{packageId}", packageId))
                .andExpect(status().isOk())
                // 상세 응답의 package_id가 생성한 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.package_id").value(packageId))
                // 상세 응답의 제목이 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.package_title").value("주간 패키지"))
                // 상세 응답의 두 번째 activity_id가 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.activity_ids[1]").value(101))
                // 상세 응답의 두 번째 viewer_id가 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.viewer_ids[1]").value(3));

        // 수정 요청을 통해 본문과 컬렉션 필드가 모두 갱신되는지 확인한다.
        mockMvc.perform(put("/api/activity-packages/{packageId}", packageId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "package_title": "월간 패키지",
                                  "package_description": "월간 활동 묶음",
                                  "po_id": "PO-2025-002",
                                  "activity_ids": [200, 300],
                                  "viewer_ids": [30, 40]
                                }
                                """))
                .andExpect(status().isOk())
                // 수정 응답의 package_id가 기존 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.package_id").value(packageId))
                // 수정 응답의 제목이 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.package_title").value("월간 패키지"))
                // 수정 응답의 PO ID가 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.po_id").value("PO-2025-002"))
                // 수정 응답의 첫 activity_id가 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.activity_ids[0]").value(200))
                // 수정 응답의 첫 viewer_id가 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.viewer_ids[0]").value(30));

        // 수정된 패키지 상태를 DB에 즉시 반영한다.
        activityPackageRepository.flush();

        // 목록 조회에서 수정된 제목이 노출되는지 확인한다.
        mockMvc.perform(get("/api/activity-packages").param("creatorId", "7"))
                .andExpect(status().isOk())
                // 목록 응답 첫 원소의 package_id가 수정한 패키지 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.content[0].package_id").value(packageId))
                // 목록 응답 첫 원소의 제목이 수정 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.content[0].package_title").value("월간 패키지"));

        // 삭제 요청이 204 응답으로 처리되는지 확인한다.
        mockMvc.perform(delete("/api/activity-packages/{packageId}", packageId).with(csrf()))
                .andExpect(status().isNoContent());

        // 삭제 결과를 DB에 반영한다.
        activityPackageRepository.flush();

        // 최종적으로 패키지가 DB에서 제거됐는지 확인한다.
        assertThat(activityPackageRepository.findById(packageId)).isEmpty();
    }

    @Test
    @DisplayName("필수 필드 없이 패키지 생성 시 400을 반환한다")
    void createPackage_returns400WhenTitleMissing() throws Exception {
        // package_title은 @NotBlank 필수 필드이므로 누락 시 400이어야 한다.
        mockMvc.perform(post("/api/activity-packages")
                        .with(csrf())
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        // package_title을 포함하지 않는 요청을 전송한다.
                        .content("""
                                {
                                  "package_description": "설명만 있음",
                                  "activity_ids": [1],
                                  "viewer_ids": [2]
                                }
                                """))
                // 유효성 검증 실패로 400 Bad Request가 반환되는지 확인한다.
                .andExpect(status().isBadRequest())
                // 응답 본문에 message 필드가 포함되는지 확인한다.
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("존재하지 않는 패키지 단건 조회 시 404를 반환한다")
    void getPackage_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 단건 조회 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(get("/api/activity-packages/{packageId}", 99999L))
                // 패키지 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("활동 패키지를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 패키지 수정 시 404를 반환한다")
    void updatePackage_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 수정 시도 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(put("/api/activity-packages/{packageId}", 99999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        // 유효한 수정 요청 본문을 전송해 존재 여부에서 실패하도록 한다.
                        .content("""
                                {
                                  "package_title": "없는 패키지 수정"
                                }
                                """))
                // 패키지 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("활동 패키지를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 패키지 삭제 시 404를 반환한다")
    void deletePackage_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 삭제 시도 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(delete("/api/activity-packages/{packageId}", 99999L).with(csrf()))
                // 패키지 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("활동 패키지를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("패키지 PDF 보고서 다운로드 시 application/pdf 응답을 반환한다")
    void downloadPackageReport_returnsPdf() throws Exception {
        // 문서 서비스가 PO 번호를 반환하도록 목 응답을 설정한다.
        given(documentsFeignClient.getPurchaseOrder("PO-REPORT-001"))
                .willReturn(new PurchaseOrderResponse("PO-REPORT-001", "PO12345", "APPROVED"));
        // 패키지 작성자 이름 조회용 목 응답을 설정한다.
        given(authFeignClient.getUser(7L))
                .willReturn(new UserResponse(7L, "패키지 작성자", "package@example.com"));
        // 활동 작성자 이름 조회용 목 응답을 설정한다.
        given(authFeignClient.getUser(10L))
                .willReturn(new UserResponse(10L, "활동 작성자", "activity@example.com"));

        // 패키지에 포함할 첫 번째 활동을 생성한다.
        MvcResult meetingResult = mockMvc.perform(post("/api/activities")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "client_id": 1,
                                  "activity_date": "2025-04-01",
                                  "activity_type": "MEETING",
                                  "activity_title": "보고용 미팅"
                                }
                                """))
                // 활동 생성이 성공하는지 확인한다.
                .andExpect(status().isCreated())
                // 응답 본문에 activity_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_id").exists())
                .andReturn();
        // 첫 번째 활동 ID를 응답에서 추출한다.
        long meetingActivityId = extractLong(meetingResult, "activity_id");

        // 패키지에 포함할 두 번째 활동을 일정 유형으로 생성한다.
        MvcResult scheduleResult = mockMvc.perform(post("/api/activities")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "client_id": 1,
                                  "activity_date": "2025-04-02",
                                  "activity_type": "SCHEDULE",
                                  "activity_title": "보고용 일정"
                                }
                                """))
                // 활동 생성이 성공하는지 확인한다.
                .andExpect(status().isCreated())
                // 응답 본문에 activity_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_id").exists())
                .andReturn();
        // 두 번째 활동 ID를 응답에서 추출한다.
        long scheduleActivityId = extractLong(scheduleResult, "activity_id");

        // 일정 활동에 시작일과 종료일을 채워 보고서 표시 데이터를 완성한다.
        mockMvc.perform(put("/api/activities/{activityId}", scheduleActivityId)
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activity_date": "2025-04-02",
                                  "activity_type": "SCHEDULE",
                                  "activity_title": "보고용 일정",
                                  "activity_content": "PDF 일정 테스트",
                                  "po_id": "PO-REPORT-001",
                                  "activity_priority": null,
                                  "activity_schedule_from": "2025-04-10",
                                  "activity_schedule_to": "2025-04-11"
                                }
                                """))
                // 일정 업데이트가 성공하는지 확인한다.
                .andExpect(status().isOk());

        // PDF 대상으로 사용할 패키지를 생성한다.
        MvcResult packageResult = mockMvc.perform(post("/api/activity-packages")
                        .with(csrf())
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "package_title": "PDF 보고서 패키지",
                                  "package_description": "보고서 다운로드 테스트",
                                  "po_id": "PO-REPORT-001",
                                  "activity_ids": [%d, %d],
                                  "viewer_ids": [2, 3]
                                }
                                """.formatted(meetingActivityId, scheduleActivityId)))
                // 패키지 생성이 성공하는지 확인한다.
                .andExpect(status().isCreated())
                // 응답 본문에 package_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.package_id").exists())
                .andReturn();
        // PDF 다운로드에 사용할 package_id를 응답에서 추출한다.
        long packageId = extractLong(packageResult, "package_id");

        // 생성한 패키지에 대한 PDF 다운로드 요청을 수행한다.
        MvcResult reportResult = mockMvc.perform(get("/api/activity-packages/{packageId}/report", packageId))
                // 응답 상태가 200 OK인지 확인한다.
                .andExpect(status().isOk())
                // 응답 MIME 타입이 application/pdf인지 확인한다.
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                // 다운로드용 attachment 헤더가 포함되는지 확인한다.
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andReturn();

        // 응답 본문의 PDF 바이트 배열을 꺼낸다.
        byte[] pdfBytes = reportResult.getResponse().getContentAsByteArray();
        // 다운로드 파일명 검증을 위해 Content-Disposition 헤더를 읽는다.
        String contentDisposition = reportResult.getResponse().getHeader("Content-Disposition");
        // 다운로드 헤더를 파싱 가능한 ContentDisposition 객체로 변환한다.
        ContentDisposition parsedContentDisposition = ContentDisposition.parse(contentDisposition);
        // PDF 바이트 배열이 비어 있지 않은지 확인한다.
        assertThat(pdfBytes).isNotEmpty();
        // 다운로드 파일명이 패키지 제목 기반으로 생성됐는지 확인한다.
        assertThat(parsedContentDisposition.getFilename()).isEqualTo("PDF 보고서 패키지.pdf");
        // PDF 시그니처 검증을 위해 앞 5바이트를 ASCII 문자열로 변환한다.
        String pdfSignature = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        // 생성된 응답이 실제 PDF 파일 시그니처를 가지는지 확인한다.
        assertThat(pdfSignature).isEqualTo("%PDF-");
    }
}
