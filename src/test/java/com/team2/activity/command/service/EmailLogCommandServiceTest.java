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

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailLogCommandService 테스트")
class EmailLogCommandServiceTest {

    @Mock
    private EmailLogRepository emailLogRepository;

    @InjectMocks
    private EmailLogCommandService emailLogCommandService;

    private EmailLog buildEmailLog() {
        return EmailLog.builder()
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
    @DisplayName("이메일 로그 생성 시 repository save 결과를 반환한다")
    void createEmailLog_returnsSavedEmailLog() {
        EmailLog emailLog = buildEmailLog();
        when(emailLogRepository.save(emailLog)).thenReturn(emailLog);

        EmailLog result = emailLogCommandService.createEmailLog(emailLog);

        assertThat(result).isSameAs(emailLog);
        verify(emailLogRepository).save(emailLog);
    }

    @Test
    @DisplayName("이메일 상태 수정 시 조회한 엔티티의 상태를 변경한다")
    void updateStatus_updatesLoadedEntity() {
        EmailLog emailLog = buildEmailLog();
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        EmailLog result = emailLogCommandService.updateStatus(1L, MailStatus.FAILED);

        assertThat(result).isSameAs(emailLog);
        assertThat(emailLog.getEmailStatus()).isEqualTo(MailStatus.FAILED);
        verify(emailLogRepository).findById(1L);
    }

    @Test
    @DisplayName("수정 대상 이메일 로그가 없으면 예외를 던진다")
    void updateStatus_throwsWhenEmailLogDoesNotExist() {
        when(emailLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailLogCommandService.updateStatus(999L, MailStatus.FAILED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이메일 로그 삭제 시 조회한 엔티티를 삭제한다")
    void deleteEmailLog_deletesLoadedEntity() {
        EmailLog emailLog = buildEmailLog();
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(emailLog));

        emailLogCommandService.deleteEmailLog(1L);

        verify(emailLogRepository).findById(1L);
        verify(emailLogRepository).delete(emailLog);
    }
}
