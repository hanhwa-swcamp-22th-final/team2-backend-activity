package com.team2.activity.integration;

import com.team2.activity.command.domain.repository.EmailLogRepository;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// EmailLog API의 생성, 조회, 상태 필터, 재전송 흐름을 통합 레벨에서 검증한다.
@DisplayName("EmailLog 통합 테스트")
class EmailLogIntegrationTest extends IntegrationTestSupport {

    // 재전송 이후 DB 상태를 직접 확인할 repository다.
    @Autowired
    private EmailLogRepository emailLogRepository;

    // 1차 캐시 초기화에 사용할 EntityManager다.
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("이메일 로그 생성 후 상세 조회와 상태 필터 조회를 검증한다")
    void createAndQueryEmailLogFlow() throws Exception {
        // 이메일 로그 생성 요청을 보내고 생성된 email_log_id를 추출한다.
        MvcResult createResult = mockMvc.perform(post("/api/email-logs")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "client_id": 1,
                                  "po_id": "PO-001",
                                  "email_title": "안내 메일",
                                  "email_recipient_name": "고객",
                                  "email_recipient_email": "client@example.com",
                                  "doc_types": ["PI", "CI"]
                                }
                                """))
                .andExpect(status().isCreated())
                // 생성 응답에 email_log_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.email_log_id").exists())
                // 생성 응답에 email_sender_id가 헤더 값으로 반영됐는지 확인한다.
                .andExpect(jsonPath("$.email_sender_id").value(10))
                // document 서비스가 발송을 담당하므로 생성 직후 상태는 PENDING이다.
                .andExpect(jsonPath("$.email_status").value("PENDING"))
                // 생성 응답의 첫 문서 유형이 PI인지 확인한다.
                .andExpect(jsonPath("$.doc_types[0].email_doc_type").value("PI"))
                .andReturn();

        // 후속 조회에 사용할 email_log_id를 응답 본문에서 읽어 온다.
        long emailLogId = extractLong(createResult, "email_log_id");

        // 생성된 이메일 로그가 상세 조회 API에서 조회되는지 확인한다.
        mockMvc.perform(get("/api/email-logs/{emailLogId}", emailLogId))
                .andExpect(status().isOk())
                // 상세 응답의 email_log_id가 생성한 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.email_log_id").value(emailLogId))
                // 상세 응답의 제목이 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.email_title").value("안내 메일"))
                // 상세 응답의 두 번째 문서 유형이 CI인지 확인한다.
                .andExpect(jsonPath("$.doc_types[1].email_doc_type").value("CI"));

        // JPA INSERT를 MyBatis SELECT 전에 DB에 반영한다.
        emailLogRepository.flush();

        // 상태 필터 목록 조회에서 생성된 메일이 노출되는지 확인한다.
        mockMvc.perform(get("/api/email-logs").param("emailStatus", "PENDING"))
                .andExpect(status().isOk())
                // 목록 첫 원소의 email_log_id가 생성한 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.content[0].email_log_id").value(emailLogId))
                // 목록 첫 원소의 상태가 PENDING인지 확인한다.
                .andExpect(jsonPath("$.content[0].email_status").value("PENDING"));
    }

    @Test
    @DisplayName("실패한 이메일은 재전송되고 이미 발송된 이메일은 409를 반환한다")
    void resendFlow() throws Exception {
        // FAILED 상태의 이메일 로그를 DB에 직접 저장한다.
        EmailLog failedLog = emailLogRepository.save(EmailLog.builder()
                // 테스트용 거래처 ID를 설정한다.
                .clientId(2L)
                // 테스트용 PO ID를 설정한다.
                .poId("PO-002")
                // 테스트용 이메일 제목을 설정한다.
                .emailTitle("재전송 대상")
                // 테스트용 수신자 이름을 설정한다.
                .emailRecipientName("고객")
                // 테스트용 수신자 이메일을 설정한다.
                .emailRecipientEmail("retry@example.com")
                // 테스트용 발송자 ID를 설정한다.
                .emailSenderId(10L)
                // 테스트용 메일 상태를 FAILED로 설정한다.
                .emailStatus(MailStatus.FAILED)
                // FAILED 상태 EmailLog 저장용 픽스처 생성을 마무리한다.
                .build());

        // document 서비스가 재전송 성공 응답을 반환하도록 mock을 설정한다.
        given(documentsFeignClient.sendEmail(eq(10L), any()))
                .willReturn(new com.team2.activity.command.infrastructure.client.EmailSendResponse(
                        "SENT", "Email sent successfully", List.of()));

        // 실패 로그 재전송 시 상태가 SENT로 바뀌는지 확인한다.
        mockMvc.perform(post("/api/email-logs/{emailLogId}/resend", failedLog.getEmailLogId())
                        .header("X-User-Id", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                // 재전송 응답의 email_log_id가 실패 로그 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.email_log_id").value(failedLog.getEmailLogId()))
                // 목 발송 성공으로 SENT 상태가 됐는지 확인한다.
                .andExpect(jsonPath("$.email_status").value("SENT"));

        // JPA UPDATE를 DB에 즉시 반영한다.
        emailLogRepository.flush();
        // 1차 캐시를 비워 DB에서 최신 상태를 다시 읽도록 한다.
        entityManager.clear();

        // DB에도 실제로 상태 변경이 반영됐는지 확인한다.
        assertThat(emailLogRepository.findById(failedLog.getEmailLogId()))
                .get()
                // DB에서 재조회한 엔티티의 메일 상태 필드를 추출한다.
                .extracting(EmailLog::getEmailStatus)
                // DB에도 SENT 상태가 반영됐는지 확인한다.
                .isEqualTo(MailStatus.SENT);

        // 이미 SENT 상태인 이메일 로그를 추가로 저장한다.
        EmailLog sentLog = emailLogRepository.save(EmailLog.builder()
                // 테스트용 거래처 ID를 설정한다.
                .clientId(3L)
                // 테스트용 PO ID를 설정한다.
                .poId("PO-003")
                // 테스트용 이메일 제목을 설정한다.
                .emailTitle("이미 발송됨")
                // 테스트용 수신자 이름을 설정한다.
                .emailRecipientName("고객")
                // 테스트용 수신자 이메일을 설정한다.
                .emailRecipientEmail("sent@example.com")
                // 테스트용 발송자 ID를 설정한다.
                .emailSenderId(11L)
                // 테스트용 메일 상태를 SENT로 설정한다.
                .emailStatus(MailStatus.SENT)
                // SENT 상태 EmailLog 저장용 픽스처 생성을 마무리한다.
                .build());

        // 이미 발송된 메일 재전송 요청은 409 충돌 응답이어야 한다.
        mockMvc.perform(post("/api/email-logs/{emailLogId}/resend", sentLog.getEmailLogId())
                        .header("X-User-Id", "11")
                        .with(csrf()))
                .andExpect(status().isConflict())
                // 충돌 응답 메시지가 기대값과 같은지 확인한다.
                .andExpect(jsonPath("$.message").value("이미 발송된 이메일입니다."));
    }

    @Test
    @DisplayName("필수 필드 없이 이메일 로그 생성 시 400을 반환한다")
    void createEmailLog_returns400WhenRequiredFieldsMissing() throws Exception {
        // email_recipient_email은 @NotBlank 필수 필드이므로 누락 시 400이어야 한다.
        mockMvc.perform(post("/api/email-logs")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        // email_recipient_email을 포함하지 않는 요청을 전송한다.
                        .content("""
                                {
                                  "client_id": 1,
                                  "po_id": "PO-001",
                                  "email_title": "제목"
                                }
                                """))
                // 유효성 검증 실패로 400 Bad Request가 반환되는지 확인한다.
                .andExpect(status().isBadRequest())
                // 응답 본문에 message 필드가 포함되는지 확인한다.
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("존재하지 않는 이메일 로그 단건 조회 시 404를 반환한다")
    void getEmailLog_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 단건 조회 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(get("/api/email-logs/{emailLogId}", 99999L))
                // 이메일 로그 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("이메일 로그를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 이메일 로그 재전송 시 404를 반환한다")
    void resend_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 재전송 시도 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(post("/api/email-logs/{emailLogId}/resend", 99999L)
                        .header("X-User-Id", "10")
                        .with(csrf()))
                // 이메일 로그 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("이메일 로그를 찾을 수 없습니다."));
    }
}
