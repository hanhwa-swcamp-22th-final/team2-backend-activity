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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// EmailLogCommandService가 로그 저장, 재전송, 내부 수신을 올바르게 처리하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailLogCommandService 테스트")
class EmailLogCommandServiceTest {

    // 이메일 로그 저장소 역할을 하는 repository 목 객체다.
    @Mock
    private EmailLogRepository emailLogRepository;

    // 재전송 시 document 서비스 호출을 시뮬레이션할 목 객체다.
    @Mock
    private DocumentsFeignClient documentsFeignClient;

    // repository와 documentsFeignClient를 주입받는 이메일 로그 command 서비스다.
    @InjectMocks
    private EmailLogCommandService emailLogCommandService;

    // 공통 이메일 로그 픽스처를 생성한다.
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

    @Test
    @DisplayName("이메일 로그 생성 시 저장 후 repository save 결과를 반환한다")
    void createEmailLog_returnsSavedEmailLog() {
        // 저장할 이메일 로그와 save 결과를 준비한다.
        EmailLog emailLog = buildEmailLog(MailStatus.SENT);
        // repository save 호출 시 같은 이메일 로그를 반환하도록 설정한다.
        when(emailLogRepository.save(emailLog)).thenReturn(emailLog);

        // 서비스가 저장 결과를 반환하는지 확인한다.
        EmailLog result = emailLogCommandService.createEmailLog(emailLog);

        // 반환 결과가 repository가 돌려준 이메일 로그와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(emailLog);
        // save가 정확히 한 번 호출됐는지 검증한다.
        verify(emailLogRepository).save(emailLog);
    }

    @Test
    @DisplayName("document 서비스 내부 요청으로 이메일 로그를 저장한다")
    void createEmailLogInternal_savesLog() {
        // document 서비스에서 전달하는 내부 요청을 구성한다.
        EmailLogInternalRequest request = new EmailLogInternalRequest(
                1L, "PO-001", "견적서", "김고객", "client@example.com",
                10L, "sent", List.of(), List.of(), List.of()
        );
        // 내부 요청으로 생성될 엔티티를 준비한다.
        EmailLog emailLog = request.toEntity();
        when(emailLogRepository.save(any())).thenReturn(emailLog);

        // 내부 요청 처리 후 저장된 로그가 반환되는지 확인한다.
        EmailLog result = emailLogCommandService.createEmailLogInternal(request);

        // save가 호출됐는지 검증한다.
        verify(emailLogRepository).save(any());
        // 저장된 로그의 상태가 SENT인지 확인한다.
        assertThat(result.getEmailStatus()).isEqualTo(MailStatus.SENT);
    }

    @Test
    @DisplayName("FAILED 이메일 재전송 시 document 서비스 호출 후 상태를 SENT로 변경한다")
    void resend_callsDocumentServiceAndUpdatesSentStatus() {
        // FAILED 상태의 이메일 로그를 조회하도록 설정한다.
        EmailLog emailLog = buildEmailLog(MailStatus.FAILED);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));
        // document 서비스가 발송 성공 응답을 반환하도록 설정한다.
        when(documentsFeignClient.sendEmail(any()))
                .thenReturn(new EmailSendResponse("SENT", "Email sent successfully", List.of()));

        // 재전송 시 상태가 SENT로 변경되는지 확인한다.
        EmailLog result = emailLogCommandService.resend(1L, 7L);

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(emailLog);
        // document 서비스 호출 후 상태가 SENT로 변경됐는지 확인한다.
        assertThat(emailLog.getEmailStatus()).isEqualTo(MailStatus.SENT);
        // markAsSent() 호출로 발송 시각이 기록됐는지 확인한다.
        assertThat(emailLog.getEmailSentAt()).isNotNull();
        // findById가 호출됐는지 검증한다.
        verify(emailLogRepository).findById(1L);
        // document 서비스가 호출됐는지 검증한다.
        verify(documentsFeignClient).sendEmail(any());
    }

    @Test
    @DisplayName("document 서비스가 FAILED 응답을 반환하면 재전송 실패 예외를 던진다")
    void resend_throwsWhenDocumentServiceReturnsFailed() {
        // FAILED 상태의 이메일 로그를 조회하도록 설정한다.
        EmailLog emailLog = buildEmailLog(MailStatus.FAILED);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));
        // document 서비스가 발송 실패 응답을 반환하도록 설정한다.
        when(documentsFeignClient.sendEmail(any()))
                .thenReturn(new EmailSendResponse("FAILED", "No documents could be generated", List.of()));

        // document 서비스가 FAILED 응답을 반환하면 예외가 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이메일 재전송에 실패했습니다.");
    }

    @Test
    @DisplayName("document 서비스 호출 실패 시 재전송 실패 예외를 던진다")
    void resend_throwsWhenDocumentServiceThrows() {
        // FAILED 상태의 이메일 로그를 조회하도록 설정한다.
        EmailLog emailLog = buildEmailLog(MailStatus.FAILED);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));
        // document 서비스 호출 시 네트워크 장애를 시뮬레이션한다.
        when(documentsFeignClient.sendEmail(any()))
                .thenThrow(new RuntimeException("document 서비스 연결 실패"));

        // document 서비스 예외 발생 시 재전송 실패 예외가 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이메일 재전송에 실패했습니다.");
    }

    @Test
    @DisplayName("이미 발송된 이메일을 재전송하면 예외를 던진다")
    void resend_throwsWhenAlreadySent() {
        // 이미 발송된 로그는 재전송 대상이 아니므로 예외가 발생해야 한다.
        EmailLog emailLog = buildEmailLog(MailStatus.SENT);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        // 이미 발송된 메일 재전송 시 IllegalStateException이 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 발송된 이메일입니다.");
    }

    @Test
    @DisplayName("PENDING 상태 이메일을 재전송하면 예외를 던진다")
    void resend_throwsWhenPending() {
        // 아직 발송 시도 전(PENDING) 상태의 이메일은 재전송 대상이 아니다.
        EmailLog emailLog = buildEmailLog(MailStatus.PENDING);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        // PENDING 메일 재전송 시 IllegalStateException이 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.resend(1L, 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("아직 발송 시도 전인 이메일입니다.");
    }

    @Test
    @DisplayName("재전송 대상 이메일 로그가 없으면 예외를 던진다")
    void resend_throwsWhenEmailLogDoesNotExist() {
        // 조회 결과가 없으면 재전송도 예외여야 한다.
        when(emailLogRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 메일 재전송 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.resend(999L, 7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이메일 로그 삭제 시 조회한 엔티티를 삭제한다")
    void deleteEmailLog_deletesLoadedEntity() {
        // 삭제 대상 이메일 로그를 조회하도록 설정한다.
        EmailLog emailLog = buildEmailLog(MailStatus.SENT);
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        // 서비스가 조회 후 delete를 호출하는지 확인한다.
        emailLogCommandService.deleteEmailLog(1L);

        // 삭제 전에 findById가 호출됐는지 검증한다.
        verify(emailLogRepository).findById(1L);
        // 조회된 이메일 로그가 delete 대상으로 전달됐는지 검증한다.
        verify(emailLogRepository).delete(emailLog);
    }

    @Test
    @DisplayName("삭제 대상 이메일 로그가 없으면 예외를 던진다")
    void deleteEmailLog_throwsWhenEmailLogDoesNotExist() {
        // 조회 결과가 없으면 삭제 요청도 예외여야 한다.
        when(emailLogRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 메일 삭제 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.deleteEmailLog(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }
}
