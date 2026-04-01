package com.team2.activity.query.controller;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.query.service.EmailLogQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// EmailLog 조회 API의 목록/상세/예외 응답을 검증한다.
@WebMvcTest(EmailLogQueryController.class)
@WithMockUser
@DisplayName("EmailLogQueryController 테스트")
class EmailLogQueryControllerTest {

    // 컨트롤러 요청을 수행하는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 호출할 이메일 로그 조회 서비스 목 객체다.
    @MockBean
    private EmailLogQueryService emailLogQueryService;

    // 응답 검증에 사용할 공통 이메일 로그 픽스처를 생성한다.
    private EmailLog buildEmailLog() {
        return EmailLog.builder()
                // 테스트용 이메일 로그 ID를 설정한다.
                .emailLogId(1L)
                // 테스트용 거래처 ID를 설정한다.
                .clientId(1L)
                // 테스트용 PO ID를 설정한다.
                .poId("PO-001")
                // 테스트용 이메일 제목을 설정한다.
                .emailTitle("견적서 발송")
                // 테스트용 수신자 이름을 설정한다.
                .emailRecipientName("김고객")
                // 테스트용 수신자 이메일을 설정한다.
                .emailRecipientEmail("client@example.com")
                // 테스트용 발송자 ID를 설정한다.
                .emailSenderId(10L)
                // 테스트용 메일 상태를 설정한다.
                .emailStatus(MailStatus.SENT)
                // 공통 EmailLog 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("GET /api/email-logs → 200 OK, 페이징 응답 구조 포함")
    void getEmailLogs_returns200WithPagedResult() throws Exception {
        // 전체 이메일 로그 조회 결과를 서비스가 반환하도록 설정한다.
        when(emailLogQueryService.getAllEmailLogs()).thenReturn(List.of(buildEmailLog()));

        // 목록 응답이 페이징 래퍼 구조를 포함하는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/email-logs"))
                .andExpect(status().isOk())
                // content 필드가 배열인지 확인한다.
                .andExpect(jsonPath("$.content").isArray())
                // total_elements 필드가 존재하는지 확인한다.
                .andExpect(jsonPath("$.total_elements").exists())
                // total_pages 필드가 존재하는지 확인한다.
                .andExpect(jsonPath("$.total_pages").exists())
                // current_page 필드가 존재하는지 확인한다.
                .andExpect(jsonPath("$.current_page").exists());
    }

    @Test
    @DisplayName("GET /api/email-logs?client_id=1 → 200 OK, client_id 필터 적용")
    void getEmailLogs_returns200WithClientIdFilter() throws Exception {
        // 거래처 조건 조회 결과를 서비스 목 객체에 등록한다.
        when(emailLogQueryService.getEmailLogsByClientId(1L)).thenReturn(List.of(buildEmailLog()));

        // 거래처 조건 목록 요청이 정상 응답으로 처리되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/email-logs").param("clientId", "1"))
                .andExpect(status().isOk())
                // content 필드가 배열인지 확인한다.
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/email-logs?email_status=FAILED → 200 OK, 상태 필터 적용")
    void getEmailLogs_returns200WithStatusFilter() throws Exception {
        // 상태 조건 조회 결과를 서비스가 반환하도록 설정한다.
        when(emailLogQueryService.getEmailLogsByStatus(MailStatus.FAILED)).thenReturn(List.of());

        // 상태 필터 요청이 빈 목록이어도 정상 응답하는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/email-logs").param("emailStatus", "FAILED"))
                .andExpect(status().isOk())
                // content 필드가 배열인지 확인한다.
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/email-logs/{email_log_id} → 200 OK, 상세 필드 포함")
    void getEmailLog_returns200WithDetail() throws Exception {
        // 단건 이메일 로그 조회 결과를 준비한다.
        when(emailLogQueryService.getEmailLog(1L)).thenReturn(buildEmailLog());

        // 상세 응답에 문서 유형과 첨부파일 배열이 포함되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/email-logs/1"))
                .andExpect(status().isOk())
                // 상세 응답에 email_log_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.email_log_id").exists())
                // 상세 응답에 email_status가 포함되는지 확인한다.
                .andExpect(jsonPath("$.email_status").exists())
                // 상세 응답에 doc_types 배열이 포함되는지 확인한다.
                .andExpect(jsonPath("$.doc_types").isArray())
                // 상세 응답에 attachments 배열이 포함되는지 확인한다.
                .andExpect(jsonPath("$.attachments").isArray());
    }

    @Test
    @DisplayName("GET /api/email-logs/{email_log_id} - 존재하지 않는 ID → 404 Not Found")
    void getEmailLog_returns404WhenNotFound() throws Exception {
        // 서비스 조회 실패 예외가 404로 변환되는지 검증한다.
        when(emailLogQueryService.getEmailLog(999L))
                .thenThrow(new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(get("/api/email-logs/999"))
                .andExpect(status().isNotFound());
    }
}
