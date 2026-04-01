package com.team2.activity.command.service;

import com.team2.activity.command.repository.EmailLogRepository;
import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// EmailLogCommandService가 생성, 상태 변경, 재전송을 올바르게 처리하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailLogCommandService 테스트")
class EmailLogCommandServiceTest {

    // 이메일 로그 저장소 역할을 하는 repository 목 객체다.
    @Mock
    private EmailLogRepository emailLogRepository;

    // repository를 호출하는 이메일 로그 command 서비스다.
    @InjectMocks
    private EmailLogCommandService emailLogCommandService;

    // 공통 이메일 로그 픽스처를 생성한다.
    private EmailLog buildEmailLog() {
        return EmailLog.builder()
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
    @DisplayName("이메일 로그 생성 시 repository save 결과를 반환한다")
    void createEmailLog_returnsSavedEmailLog() {
        // 저장할 이메일 로그와 save 결과를 준비한다.
        EmailLog emailLog = buildEmailLog();
        // repository save 호출 시 같은 이메일 로그를 반환하도록 설정한다.
        when(emailLogRepository.save(emailLog)).thenReturn(emailLog);

        // 서비스가 저장 결과를 그대로 반환하는지 확인한다.
        EmailLog result = emailLogCommandService.createEmailLog(emailLog);

        // 반환 결과가 repository가 돌려준 이메일 로그와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(emailLog);
        // save가 정확히 한 번 호출됐는지 검증한다.
        verify(emailLogRepository).save(emailLog);
    }

    @Test
    @DisplayName("발송 실패 이메일 재전송 시 상태를 SENT로 변경한다")
    void resend_updatesStatusWhenFailed() {
        // FAILED 상태의 이메일 로그를 조회하도록 설정한다.
        EmailLog emailLog = EmailLog.builder()
                // 테스트용 거래처 ID를 설정한다.
                .clientId(1L).poId("PO-001").emailTitle("견적서 발송")
                // 테스트용 수신자 이름과 이메일을 설정한다.
                .emailRecipientName("김고객").emailRecipientEmail("client@example.com")
                // 테스트용 발송자 ID와 FAILED 상태를 설정한다.
                .emailSenderId(10L).emailStatus(MailStatus.FAILED)
                // FAILED 상태 EmailLog 픽스처 생성을 마무리한다.
                .build();
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        // 재전송 시 상태가 SENT로 변경되는지 확인한다.
        EmailLog result = emailLogCommandService.resend(1L);

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(emailLog);
        // 상태가 SENT로 변경됐는지 확인한다.
        assertThat(emailLog.getEmailStatus()).isEqualTo(MailStatus.SENT);
        // 재전송 전에 findById가 호출됐는지 검증한다.
        verify(emailLogRepository).findById(1L);
    }

    @Test
    @DisplayName("이미 발송된 이메일을 재전송하면 예외를 던진다")
    void resend_throwsWhenAlreadySent() {
        // 이미 발송된 로그는 재전송 대상이 아니므로 예외가 발생해야 한다.
        EmailLog emailLog = buildEmailLog(); // emailStatus = SENT
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        // 이미 발송된 메일 재전송 시 IllegalStateException이 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.resend(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 발송된 이메일입니다.");
    }

    @Test
    @DisplayName("재전송 대상 이메일 로그가 없으면 예외를 던진다")
    void resend_throwsWhenEmailLogDoesNotExist() {
        // 조회 결과가 없으면 재전송도 예외여야 한다.
        when(emailLogRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 메일 재전송 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.resend(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이메일 상태 수정 시 조회한 엔티티의 상태를 변경한다")
    void updateStatus_updatesLoadedEntity() {
        // 상태 변경 대상 이메일 로그를 조회하도록 설정한다.
        EmailLog emailLog = buildEmailLog();
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        // 요청한 상태로 엔티티 필드가 변경되는지 확인한다.
        EmailLog result = emailLogCommandService.updateStatus(1L, MailStatus.FAILED);

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(emailLog);
        // 상태가 FAILED로 변경됐는지 확인한다.
        assertThat(emailLog.getEmailStatus()).isEqualTo(MailStatus.FAILED);
        // 상태 변경 전에 findById가 호출됐는지 검증한다.
        verify(emailLogRepository).findById(1L);
    }

    @Test
    @DisplayName("수정 대상 이메일 로그가 없으면 예외를 던진다")
    void updateStatus_throwsWhenEmailLogDoesNotExist() {
        // 조회 실패 시 상태 변경 요청도 예외여야 한다.
        when(emailLogRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 메일 상태 변경 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogCommandService.updateStatus(999L, MailStatus.FAILED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이메일 로그 삭제 시 조회한 엔티티를 삭제한다")
    void deleteEmailLog_deletesLoadedEntity() {
        // 삭제 대상 이메일 로그를 조회하도록 설정한다.
        EmailLog emailLog = buildEmailLog();
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        // 서비스가 조회 후 delete를 호출하는지 확인한다.
        emailLogCommandService.deleteEmailLog(1L);

        // 삭제 전에 findById가 호출됐는지 검증한다.
        verify(emailLogRepository).findById(1L);
        // 조회된 이메일 로그가 delete 대상으로 전달됐는지 검증한다.
        verify(emailLogRepository).delete(emailLog);
    }
}
