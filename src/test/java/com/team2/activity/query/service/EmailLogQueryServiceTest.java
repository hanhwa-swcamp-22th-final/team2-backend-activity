package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.query.dto.EmailLogResponse;
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

// EmailLogQueryService가 읽기 모델 조회를 mapper에 위임하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailLogQueryService 테스트")
class EmailLogQueryServiceTest {

    // 이메일 로그 읽기 쿼리를 수행하는 mapper 목 객체다.
    @Mock
    private EmailLogQueryMapper emailLogQueryMapper;

    // mapper를 감싸는 이메일 로그 조회 서비스다.
    @InjectMocks
    private EmailLogQueryService emailLogQueryService;

    // 이메일 로그 조회 테스트에 사용할 공통 픽스처를 만든다.
    private EmailLog buildEmailLog(Long clientId, MailStatus mailStatus) {
        return EmailLog.builder()
                // 테스트용 이메일 로그 ID를 설정한다.
                .emailLogId(1L)
                // 테스트용 거래처 ID를 설정한다.
                .clientId(clientId)
                // 테스트용 PO ID를 설정한다.
                .poId("PO-001")
                // 테스트용 이메일 제목을 설정한다.
                .emailTitle("안내 메일")
                // 테스트용 수신자 이름을 설정한다.
                .emailRecipientName("고객")
                // 테스트용 수신자 이메일을 설정한다.
                .emailRecipientEmail("client@example.com")
                // 테스트용 발송자 ID를 설정한다.
                .emailSenderId(10L)
                // 테스트용 메일 상태를 설정한다.
                .emailStatus(mailStatus)
                // 공통 EmailLog 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("단건 조회 시 mapper 결과를 반환한다")
    void getEmailLog_returnsMappedEmailLog() {
        // mapper가 반환할 이메일 로그를 준비한다.
        EmailLog emailLog = buildEmailLog(1L, MailStatus.SENT);
        // mapper findById 호출 시 같은 엔티티를 반환하도록 설정한다.
        when(emailLogQueryMapper.findById(1L)).thenReturn(emailLog);

        // 서비스가 mapper 결과를 응답 DTO로 변환해 반환하는지 확인한다.
        EmailLogResponse result = emailLogQueryService.getEmailLog(1L);

        // 반환 DTO의 이메일 로그 ID가 mapper 결과와 같은지 확인한다.
        assertThat(result.emailLogId()).isEqualTo(emailLog.getEmailLogId());
        // 반환 DTO의 거래처 ID가 mapper 결과와 같은지 확인한다.
        assertThat(result.clientId()).isEqualTo(emailLog.getClientId());
        // 반환 DTO의 PO ID가 mapper 결과와 같은지 확인한다.
        assertThat(result.poId()).isEqualTo(emailLog.getPoId());
        // 반환 DTO의 메일 상태가 mapper 결과와 같은지 확인한다.
        assertThat(result.emailStatus()).isEqualTo(emailLog.getEmailStatus());
        // findById가 정확히 한 번 호출됐는지 검증한다.
        verify(emailLogQueryMapper).findById(1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 예외를 던진다")
    void getEmailLog_throwsWhenEmailLogDoesNotExist() {
        // 조회 결과가 없으면 서비스가 예외를 던져야 한다.
        when(emailLogQueryMapper.findById(999L)).thenReturn(null);

        // 없는 이메일 로그 조회 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> emailLogQueryService.getEmailLog(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 로그를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 조회 시 mapper 목록을 그대로 반환한다")
    void getAllEmailLogs_returnsMapperResult() {
        // mapper가 반환할 이메일 로그 목록을 준비한다.
        List<EmailLog> emailLogs = List.of(
                buildEmailLog(1L, MailStatus.SENT),
                buildEmailLog(2L, MailStatus.FAILED)
        );
        // mapper findAll 호출 시 준비한 목록을 반환하도록 설정한다.
        when(emailLogQueryMapper.findAll()).thenReturn(emailLogs);

        // 서비스가 전체 조회 결과를 그대로 전달하는지 확인한다.
        List<EmailLog> result = emailLogQueryService.getAllEmailLogs();

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(emailLogs);
        // findAll이 정확히 한 번 호출됐는지 검증한다.
        verify(emailLogQueryMapper).findAll();
    }

    @Test
    @DisplayName("거래처 ID 조건 조회를 위임한다")
    void getEmailLogsByClientId_delegatesToMapper() {
        // 거래처 기준 목록을 mapper가 반환하도록 설정한다.
        List<EmailLog> emailLogs = List.of(buildEmailLog(1L, MailStatus.SENT));
        // mapper가 거래처 조건 목록을 반환하도록 설정한다.
        when(emailLogQueryMapper.findByClientId(1L)).thenReturn(emailLogs);

        // 서비스가 clientId 조건 조회를 mapper에 위임하는지 확인한다.
        List<EmailLog> result = emailLogQueryService.getEmailLogsByClientId(1L);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(emailLogs);
        // findByClientId가 정확히 한 번 호출됐는지 검증한다.
        verify(emailLogQueryMapper).findByClientId(1L);
    }

    @Test
    @DisplayName("상태 조건 조회를 위임한다")
    void getEmailLogsByStatus_delegatesToMapper() {
        // 상태 기준 목록을 mapper가 반환하도록 설정한다.
        List<EmailLog> emailLogs = List.of(buildEmailLog(1L, MailStatus.FAILED));
        // mapper가 상태 조건 목록을 반환하도록 설정한다.
        when(emailLogQueryMapper.findByEmailStatus(MailStatus.FAILED)).thenReturn(emailLogs);

        // 서비스가 상태 조건 조회를 mapper에 위임하는지 확인한다.
        List<EmailLog> result = emailLogQueryService.getEmailLogsByStatus(MailStatus.FAILED);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(emailLogs);
        // findByEmailStatus가 정확히 한 번 호출됐는지 검증한다.
        verify(emailLogQueryMapper).findByEmailStatus(MailStatus.FAILED);
    }
}
