package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.EmailLogCommandService;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// EmailLog 쓰기 API의 생성과 재전송 응답을 검증한다.
@WebMvcTest(EmailLogCommandController.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("EmailLogCommandController 테스트")
class EmailLogCommandControllerTest {

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

    // 컨트롤러가 호출할 이메일 로그 command 서비스 목 객체다.
    @MockBean
    private EmailLogCommandService emailLogCommandService;

    // 상태별 응답 검증에 사용할 공통 이메일 로그 픽스처다.
    private EmailLog buildEmailLog(MailStatus status) {
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
                .emailStatus(status)
                // 공통 EmailLog 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("POST /api/email-logs → 201 Created, email_log_id 포함")
    void createEmailLog_returns201() throws Exception {
        // 생성 요청에 대한 서비스 반환값을 준비한다.
        when(emailLogCommandService.createEmailLog(any())).thenReturn(buildEmailLog(MailStatus.SENT));

        // 유효한 생성 요청이 201 응답으로 처리되는지 확인한다.
        // 응답 상태가 201 Created인지 확인한다.
        mockMvc.perform(post("/api/email-logs")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "clientId": 1,
                                    "poId": "PO-001",
                                    "emailTitle": "견적서 발송",
                                    "emailRecipientName": "김고객",
                                    "emailRecipientEmail": "client@example.com",
                                    "docTypes": ["PI", "CI"]
                                }
                                """))
                .andExpect(status().isCreated())
                // 응답 본문에 email_log_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.emailLogId").exists())
                // 응답 본문에 email_status가 SENT로 내려오는지 확인한다.
                .andExpect(jsonPath("$.emailStatus").value("sent"))
                // 응답 본문에 email_sender_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.emailSenderId").exists());

        // 헤더의 사용자 ID가 senderId로 매핑됐는지 검증한다.
        verify(emailLogCommandService).createEmailLog(argThat(e -> Long.valueOf(10L).equals(e.getEmailSenderId())));
    }

    @Test
    @DisplayName("POST /api/email-logs - 필수 필드 누락 → 400 Bad Request")
    void createEmailLog_returns400WhenRequiredFieldMissing() throws Exception {
        // 필수 필드 누락 요청이 검증 오류로 처리되는지 확인한다.
        // 응답 상태가 400 Bad Request인지 확인한다.
        mockMvc.perform(post("/api/email-logs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/email-logs/{email_log_id}/resend - FAILED 상태 → 200 OK")
    void resendEmailLog_returns200WhenFailed() throws Exception {
        // 재전송 성공 시 SENT 상태 응답을 반환하도록 설정한다.
        when(emailLogCommandService.resend(1L, 10L)).thenReturn(buildEmailLog(MailStatus.SENT));

        // FAILED 상태 재전송 요청이 200 응답을 반환하는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(post("/api/email-logs/1/resend")
                        .header("X-User-Id", "7")
                        .with(csrf()))
                .andExpect(status().isOk())
                // 응답 본문에 email_log_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.emailLogId").exists())
                // 응답 본문에 email_status가 SENT로 내려오는지 확인한다.
                .andExpect(jsonPath("$.emailStatus").value("sent"));
    }

    @Test
    @DisplayName("POST /api/email-logs/{email_log_id}/resend - 이미 SENT 상태 → 409 Conflict")
    void resendEmailLog_returns409WhenAlreadySent() throws Exception {
        // 이미 발송된 메일 재전송 요청은 상태 충돌 예외를 반환한다.
        when(emailLogCommandService.resend(1L, 10L))
                .thenThrow(new IllegalStateException("이미 발송된 이메일입니다."));

        // 응답 상태가 409 Conflict인지 확인한다.
        mockMvc.perform(post("/api/email-logs/1/resend")
                        .header("X-User-Id", "7")
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/email-logs/{email_log_id}/resend - 존재하지 않는 ID → 404 Not Found")
    void resendEmailLog_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 메일 재전송 요청은 404 응답으로 변환돼야 한다.
        when(emailLogCommandService.resend(999L, 10L))
                .thenThrow(new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(post("/api/email-logs/999/resend")
                        .header("X-User-Id", "7")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
