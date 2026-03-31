package com.team2.activity.query.service;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import com.team2.activity.query.mapper.EmailLogQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailLogQueryService 테스트")
class EmailLogQueryServiceTest {

    @Mock
    private EmailLogQueryMapper emailLogQueryMapper;

    @InjectMocks
    private EmailLogQueryService emailLogQueryService;

    private EmailLog buildEmailLog(Long clientId, MailStatus mailStatus) {
        return EmailLog.builder()
                .clientId(clientId)
                .poId("PO-001")
                .emailTitle("안내 메일")
                .emailRecipientName("고객")
                .emailRecipientEmail("client@example.com")
                .emailSenderId(10L)
                .emailStatus(mailStatus)
                .build();
    }

    @Test
    @DisplayName("단건 조회 시 mapper 결과를 반환한다")
    void getEmailLog_returnsMappedEmailLog() {
        EmailLog emailLog = buildEmailLog(1L, MailStatus.SENT);
        when(emailLogQueryMapper.findById(1L)).thenReturn(emailLog);

        EmailLog result = emailLogQueryService.getEmailLog(1L);

        assertThat(result).isSameAs(emailLog);
        verify(emailLogQueryMapper).findById(1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 예외를 던진다")
    void getEmailLog_throwsWhenEmailLogDoesNotExist() {
        when(emailLogQueryMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> emailLogQueryService.getEmailLog(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 조회 시 mapper 목록을 그대로 반환한다")
    void getAllEmailLogs_returnsMapperResult() {
        List<EmailLog> emailLogs = List.of(
                buildEmailLog(1L, MailStatus.SENT),
                buildEmailLog(2L, MailStatus.FAILED)
        );
        when(emailLogQueryMapper.findAll()).thenReturn(emailLogs);

        List<EmailLog> result = emailLogQueryService.getAllEmailLogs();

        assertThat(result).isEqualTo(emailLogs);
        verify(emailLogQueryMapper).findAll();
    }

    @Test
    @DisplayName("거래처 ID 조건 조회를 위임한다")
    void getEmailLogsByClientId_delegatesToMapper() {
        List<EmailLog> emailLogs = List.of(buildEmailLog(1L, MailStatus.SENT));
        when(emailLogQueryMapper.findByClientId(1L)).thenReturn(emailLogs);

        List<EmailLog> result = emailLogQueryService.getEmailLogsByClientId(1L);

        assertThat(result).isEqualTo(emailLogs);
        verify(emailLogQueryMapper).findByClientId(1L);
    }

    @Test
    @DisplayName("상태 조건 조회를 위임한다")
    void getEmailLogsByStatus_delegatesToMapper() {
        List<EmailLog> emailLogs = List.of(buildEmailLog(1L, MailStatus.FAILED));
        when(emailLogQueryMapper.findByEmailStatus(MailStatus.FAILED)).thenReturn(emailLogs);

        List<EmailLog> result = emailLogQueryService.getEmailLogsByStatus(MailStatus.FAILED);

        assertThat(result).isEqualTo(emailLogs);
        verify(emailLogQueryMapper).findByEmailStatus(MailStatus.FAILED);
    }
}
