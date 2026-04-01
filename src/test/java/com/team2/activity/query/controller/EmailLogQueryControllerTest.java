package com.team2.activity.query.controller;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
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

@WebMvcTest(EmailLogQueryController.class)
@WithMockUser
@DisplayName("EmailLogQueryController 테스트")
class EmailLogQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailLogQueryService emailLogQueryService;

    private EmailLog buildEmailLog() {
        return EmailLog.builder()
                .emailLogId(1L)
                .clientId(1L)
                .poId("PO-001")
                .emailTitle("견적서 발송")
                .emailRecipientName("김고객")
                .emailRecipientEmail("client@example.com")
                .emailSenderId(10L)
                .emailStatus(MailStatus.SENT)
                .build();
    }

    @Test
    @DisplayName("GET /api/email-logs → 200 OK, 페이징 응답 구조 포함")
    void getEmailLogs_returns200WithPagedResult() throws Exception {
        when(emailLogQueryService.getAllEmailLogs()).thenReturn(List.of(buildEmailLog()));

        mockMvc.perform(get("/api/email-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.total_elements").exists())
                .andExpect(jsonPath("$.total_pages").exists())
                .andExpect(jsonPath("$.current_page").exists());
    }

    @Test
    @DisplayName("GET /api/email-logs?client_id=1 → 200 OK, client_id 필터 적용")
    void getEmailLogs_returns200WithClientIdFilter() throws Exception {
        when(emailLogQueryService.getEmailLogsByClientId(1L)).thenReturn(List.of(buildEmailLog()));

        mockMvc.perform(get("/api/email-logs").param("client_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/email-logs?email_status=FAILED → 200 OK, 상태 필터 적용")
    void getEmailLogs_returns200WithStatusFilter() throws Exception {
        when(emailLogQueryService.getEmailLogsByStatus(MailStatus.FAILED)).thenReturn(List.of());

        mockMvc.perform(get("/api/email-logs").param("email_status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/email-logs/{email_log_id} → 200 OK, 상세 필드 포함")
    void getEmailLog_returns200WithDetail() throws Exception {
        when(emailLogQueryService.getEmailLog(1L)).thenReturn(buildEmailLog());

        mockMvc.perform(get("/api/email-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email_log_id").exists())
                .andExpect(jsonPath("$.email_status").exists())
                .andExpect(jsonPath("$.doc_types").isArray())
                .andExpect(jsonPath("$.attachments").isArray());
    }

    @Test
    @DisplayName("GET /api/email-logs/{email_log_id} - 존재하지 않는 ID → 404 Not Found")
    void getEmailLog_returns404WhenNotFound() throws Exception {
        when(emailLogQueryService.getEmailLog(999L))
                .thenThrow(new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/email-logs/999"))
                .andExpect(status().isNotFound());
    }
}
