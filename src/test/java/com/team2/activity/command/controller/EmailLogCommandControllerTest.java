package com.team2.activity.command.controller;

import com.team2.activity.command.service.EmailLogCommandService;
import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailLogCommandController.class)
@WithMockUser
@DisplayName("EmailLogCommandController 테스트")
class EmailLogCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailLogCommandService emailLogCommandService;

    private EmailLog buildEmailLog(MailStatus status) {
        return EmailLog.builder()
                .emailLogId(1L)
                .clientId(1L)
                .poId("PO-001")
                .emailTitle("견적서 발송")
                .emailRecipientName("김고객")
                .emailRecipientEmail("client@example.com")
                .emailSenderId(10L)
                .emailStatus(status)
                .build();
    }

    @Test
    @DisplayName("POST /api/email-logs → 201 Created, email_log_id 포함")
    void createEmailLog_returns201() throws Exception {
        when(emailLogCommandService.createEmailLog(any())).thenReturn(buildEmailLog(MailStatus.SENT));

        mockMvc.perform(post("/api/email-logs")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "client_id": 1,
                                    "po_id": "PO-001",
                                    "email_title": "견적서 발송",
                                    "email_recipient_name": "김고객",
                                    "email_recipient_email": "client@example.com",
                                    "doc_types": ["PI", "CI"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email_log_id").exists())
                .andExpect(jsonPath("$.email_status").value("SENT"))
                .andExpect(jsonPath("$.email_sender_id").exists());

        verify(emailLogCommandService).createEmailLog(argThat(e -> Long.valueOf(10L).equals(e.getEmailSenderId())));
    }

    @Test
    @DisplayName("POST /api/email-logs - 필수 필드 누락 → 400 Bad Request")
    void createEmailLog_returns400WhenRequiredFieldMissing() throws Exception {
        mockMvc.perform(post("/api/email-logs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/email-logs/{email_log_id}/resend - FAILED 상태 → 200 OK")
    void resendEmailLog_returns200WhenFailed() throws Exception {
        when(emailLogCommandService.resend(1L)).thenReturn(buildEmailLog(MailStatus.SENT));

        mockMvc.perform(post("/api/email-logs/1/resend")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email_log_id").exists())
                .andExpect(jsonPath("$.email_status").value("SENT"));
    }

    @Test
    @DisplayName("POST /api/email-logs/{email_log_id}/resend - 이미 SENT 상태 → 409 Conflict")
    void resendEmailLog_returns409WhenAlreadySent() throws Exception {
        when(emailLogCommandService.resend(1L))
                .thenThrow(new IllegalStateException("이미 발송된 이메일입니다."));

        mockMvc.perform(post("/api/email-logs/1/resend")
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/email-logs/{email_log_id}/resend - 존재하지 않는 ID → 404 Not Found")
    void resendEmailLog_returns404WhenNotFound() throws Exception {
        when(emailLogCommandService.resend(999L))
                .thenThrow(new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));

        mockMvc.perform(post("/api/email-logs/999/resend")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
