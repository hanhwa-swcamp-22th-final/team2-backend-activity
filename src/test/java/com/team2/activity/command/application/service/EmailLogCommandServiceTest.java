package com.team2.activity.command.application.service;

import com.team2.activity.command.application.dto.EmailLogInternalRequest;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.command.domain.repository.EmailLogRepository;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.EmailSendResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EmailLogCommandService 가 로그 저장/재전송/내부 수신을 올바르게 처리하는지 검증한다.
 *
 * <p>B1 리팩토링 후의 동작:
 * <ul>
 *   <li>resend 는 DB 레벨 원자적 상태 전이(FAILED→SENDING) 수행</li>
 *   <li>Documents 에는 {@code sendEmailWithoutLogging} 으로 호출 (이중 write 방지)</li>
 *   <li>Write Ownership: Activity 가 EmailLog 의 유일한 write owner</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailLogCommandService 테스트")
class EmailLogCommandServiceTest {

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private DocumentsFeignClient documentsFeignClient;

    @InjectMocks
    private EmailLogCommandService emailLogCommandService;

    private EmailLog buildEmailLog(MailStatus status) {
        return EmailLog.builder()
                .clientId(1L)
                .poId("PO-001")
                .emailTitle("견적서 발송")
                .emailRecipientName("김고객")
                .emailRecipientEmail("client@example.com")
                .emailSenderId(10L)
                .emailStatus(status)
                .build();
    }

    // ── create / internal ──────────────────────────────────

    @Test
    @DisplayName("이메일 로그 생성 시 저장 후 repository save 결과를 반환한다")
    void createEmailLog_returnsSavedEmailLog() {
        EmailLog emailLog = buildEmailLog(MailStatus.SENT);
        when(emailLogRepository.save(emailLog)).thenReturn(emailLog);

        EmailLog result = emailLogCommandService.createEmailLog(emailLog);

        assertThat(result).isSameAs(emailLog);
        verify(emailLogRepository).save(emailLog);
    }

    @Test
    @DisplayName("document 서비스 내부 요청으로 이메일 로그를 저장한다")
    void createEmailLogInternal_savesLog() {
        EmailLogInternalRequest request = new EmailLogInternalRequest(
                1L, "PO-001", "견적서", "김고객", "client@example.com",
                10L, "sent", List.of(), List.of(), List.of()
        );
        EmailLog emailLog = request.toEntity();
        when(emailLogRepository.save(any())).thenReturn(emailLog);

        EmailLog result = emailLogCommandService.createEmailLogInternal(request);

        verify(emailLogRepository).save(any());
        assertThat(result.getEmailStatus()).isEqualTo(MailStatus.SENT);
    }

    // ── resend 성공 경로 ────────────────────────────────────

    @Test
    @DisplayName("FAILED 이메일 재전송 시 DB-level 원자 전이 후 sendEmailWithoutLogging 호출 → SENT")
    void resend_callsDocumentServiceAndUpdatesSentStatus() {
        EmailLog emailLog = buildEmailLog(MailStatus.FAILED);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));
        // DB atomic transition 성공 (1 row updated)
        when(emailLogRepository.transitionStatus(eq(1L), eq(MailStatus.FAILED), eq(MailStatus.SENDING)))
                .thenReturn(1);
        // Documents 는 sendEmailWithoutLogging 으로 호출되어야 함
        when(documentsFeignClient.sendEmailWithoutLogging(anyLong(), any()))
                .thenReturn(new EmailSendResponse("SENT", "Email sent successfully", List.of()));

        EmailLog result = emailLogCommandService.resend(1L, 7L);

        assertThat(result).isSameAs(emailLog);
        assertThat(emailLog.getEmailStatus()).isEqualTo(MailStatus.SENT);
        assertThat(emailLog.getEmailSentAt()).isNotNull();
        verify(emailLogRepository).findById(1L);
        verify(emailLogRepository).transitionStatus(eq(1L), eq(MailStatus.FAILED), eq(MailStatus.SENDING));
        // sendEmailWithoutLogging 에 원본 sender userId (10L) 가 X-User-Id 로 전달돼야 함
        verify(documentsFeignClient).sendEmailWithoutLogging(eq(10L), any());
        // 기존 sendEmail 은 호출되지 않음 — 이중 write 방지 검증
        verify(documentsFeignClient, never()).sendEmail(any());
    }

    // ── resend 실패 경로 ────────────────────────────────────

    @Test
    @DisplayName("document 서비스가 FAILED 응답을 반환하면 재전송 실패 예외를 던진다")
    void resend_throwsWhenDocumentServiceReturnsFailed() {
        EmailLog emailLog = buildEmailLog(MailStatus.FAILED);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));
        when(emailLogRepository.transitionStatus(eq(1L), eq(MailStatus.FAILED), eq(MailStatus.SENDING)))
                .thenReturn(1);
        when(documentsFeignClient.sendEmailWithoutLogging(anyLong(), any()))
                .thenReturn(new EmailSendResponse("FAILED", "No documents could be generated", List.of()));

        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이메일 재전송에 실패했습니다.");
    }

    @Test
    @DisplayName("document 서비스 호출 실패 시 재전송 실패 예외를 던진다")
    void resend_throwsWhenDocumentServiceThrows() {
        EmailLog emailLog = buildEmailLog(MailStatus.FAILED);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));
        when(emailLogRepository.transitionStatus(eq(1L), eq(MailStatus.FAILED), eq(MailStatus.SENDING)))
                .thenReturn(1);
        when(documentsFeignClient.sendEmailWithoutLogging(anyLong(), any()))
                .thenThrow(new RuntimeException("document 서비스 연결 실패"));

        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이메일 재전송에 실패했습니다.");
    }

    // ── resend 상태 가드 ────────────────────────────────────

    @Test
    @DisplayName("이미 발송된 이메일을 재전송하면 예외를 던진다")
    void resend_throwsWhenAlreadySent() {
        EmailLog emailLog = buildEmailLog(MailStatus.SENT);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 발송된 이메일입니다.");
    }

    @Test
    @DisplayName("PENDING 상태 이메일을 재전송하면 예외를 던진다")
    void resend_throwsWhenPending() {
        EmailLog emailLog = buildEmailLog(MailStatus.PENDING);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("아직 발송 시도 전인 이메일입니다.");
    }

    @Test
    @DisplayName("SENDING 상태 이메일은 이미 재전송 진행 중이므로 예외를 던진다 (빠른 경로)")
    void resend_throwsWhenAlreadySending() {
        EmailLog emailLog = buildEmailLog(MailStatus.SENDING);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 재전송이 진행 중입니다.");
    }

    @Test
    @DisplayName("DB 원자 전이에서 경쟁 패배(0 rows) 시 재전송 진행 중 예외를 던진다")
    void resend_throwsWhenTransitionLosesRace() {
        EmailLog emailLog = buildEmailLog(MailStatus.FAILED);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));
        // 다른 요청이 먼저 FAILED → SENDING 으로 바꿨다고 가정
        when(emailLogRepository.transitionStatus(eq(1L), eq(MailStatus.FAILED), eq(MailStatus.SENDING)))
                .thenReturn(0);

        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 재전송이 진행 중");
        // Documents 호출이 아예 일어나지 않아야 함
        verify(documentsFeignClient, never()).sendEmailWithoutLogging(anyLong(), any());
    }

    @Test
    @DisplayName("재전송 대상 이메일 로그가 없으면 예외를 던진다")
    void resend_throwsWhenEmailLogDoesNotExist() {
        when(emailLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailLogCommandService.resend(999L, 7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }

    // ── delete ─────────────────────────────────────────────

    @Test
    @DisplayName("이메일 로그 삭제 시 조회한 엔티티를 삭제한다")
    void deleteEmailLog_deletesLoadedEntity() {
        EmailLog emailLog = buildEmailLog(MailStatus.SENT);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        emailLogCommandService.deleteEmailLog(1L);

        verify(emailLogRepository).findById(1L);
        verify(emailLogRepository).delete(emailLog);
    }

    @Test
    @DisplayName("삭제 대상 이메일 로그가 없으면 예외를 던진다")
    void deleteEmailLog_throwsWhenEmailLogDoesNotExist() {
        when(emailLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailLogCommandService.deleteEmailLog(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }
}
