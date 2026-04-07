package com.team2.activity.query.repository;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.EmailLogAttachment;
import com.team2.activity.command.domain.entity.EmailLogType;
import com.team2.activity.command.domain.entity.enums.DocumentType;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.query.mapper.EmailLogQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// EmailLog 조회 mapper가 본문, 문서 유형, 첨부파일을 함께 매핑하는지 검증한다.
@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@Transactional
@DisplayName("EmailLogQueryRepository 테스트")
class EmailLogQueryRepositoryTest {

    // 이메일 로그 읽기 쿼리를 수행할 실제 mapper다.
    @Autowired
    private EmailLogQueryMapper emailLogQueryMapper;

    // 이메일 로그 테스트 데이터를 직접 적재할 JDBC 도구다.
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 이메일 로그 본문과 하위 컬렉션 데이터를 저장하고 mapper 결과를 반환한다.
    private EmailLog saveEmailLog(Long clientId, MailStatus status, String title) {
        // email_logs 본문 레코드를 먼저 저장한다.
        jdbcTemplate.update(
                """
                INSERT INTO email_logs (
                    client_id, po_id, email_title, email_recipient_name, email_recipient_email,
                    email_sender_id, email_status, email_sent_at, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                clientId,
                "PO-" + clientId,
                title,
                "고객",
                "client@example.com",
                10L,
                status.getDisplayName(),
                Timestamp.valueOf(LocalDateTime.of(2025, 4, 1, 9, 0)),
                Timestamp.valueOf(LocalDateTime.of(2025, 4, 1, 9, 0))
        );

        // 방금 저장된 이메일 로그의 PK를 조회한다.
        Long emailLogId = jdbcTemplate.queryForObject("SELECT MAX(email_log_id) FROM email_logs", Long.class);

        // 문서 유형 컬렉션 데이터를 추가한다.
        jdbcTemplate.update(
                "INSERT INTO email_log_types (email_log_id, email_doc_type) VALUES (?, ?)",
                emailLogId,
                DocumentType.PI.getDisplayName()
        );
        jdbcTemplate.update(
                "INSERT INTO email_log_types (email_log_id, email_doc_type) VALUES (?, ?)",
                emailLogId,
                DocumentType.CI.getDisplayName()
        );
        // 첨부파일 컬렉션 데이터를 추가한다.
        jdbcTemplate.update(
                "INSERT INTO email_log_attachments (email_log_id, email_attachment_filename) VALUES (?, ?)",
                emailLogId,
                "PI001.pdf"
        );
        jdbcTemplate.update(
                "INSERT INTO email_log_attachments (email_log_id, email_attachment_filename) VALUES (?, ?)",
                emailLogId,
                "CI001.pdf"
        );

        // mapper 상세 조회 결과를 반환해 이후 검증에 사용한다.
        return emailLogQueryMapper.findEmailLogById(emailLogId);
    }

    @Test
    @DisplayName("ID로 이메일 로그 조회 시 문서 유형과 첨부파일이 매핑된다")
    void findById_mapsEmailLogFieldsAndCollections() {
        // 문서 유형과 첨부파일이 포함된 이메일 로그를 저장한다.
        EmailLog saved = saveEmailLog(1L, MailStatus.SENT, "견적 메일");

        // mapper가 하위 컬렉션까지 읽어 오는지 확인한다.
        EmailLog found = emailLogQueryMapper.findEmailLogById(saved.getEmailLogId());

        // 상태, 문서 유형, 첨부파일 필드가 모두 정확히 매핑되는지 검증한다.
        assertThat(found).isNotNull();
        assertThat(found.getEmailLogId()).isEqualTo(saved.getEmailLogId());
        assertThat(found.getEmailStatus()).isEqualTo(MailStatus.SENT);
        assertThat(found.getDocTypes())
                .extracting(EmailLogType::getEmailDocType)
                .containsExactlyInAnyOrder(DocumentType.PI, DocumentType.CI);
        assertThat(found.getAttachments())
                .extracting(EmailLogAttachment::getEmailAttachmentFilename)
                .containsExactlyInAnyOrder("PI001.pdf", "CI001.pdf");
    }

    @Test
    @DisplayName("거래처 ID로 이메일 로그 목록을 조회한다")
    void findByClientId_returnsOnlyMatchedEmailLogs() {
        // 서로 다른 거래처의 이메일 로그를 저장한다.
        saveEmailLog(1L, MailStatus.SENT, "고객1 메일1");
        saveEmailLog(1L, MailStatus.FAILED, "고객1 메일2");
        saveEmailLog(2L, MailStatus.SENT, "고객2 메일");

        // 거래처 조건으로 이메일 로그 목록을 조회한다.
        List<EmailLog> result = emailLogQueryMapper.findEmailLogByClientId(1L);

        // 요청한 거래처의 이메일 로그만 반환되는지 확인한다.
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(EmailLog::getClientId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("상태로 이메일 로그 목록을 조회한다")
    void findByEmailStatus_returnsOnlyMatchedEmailLogs() {
        // 서로 다른 발송 상태의 이메일 로그를 저장한다.
        saveEmailLog(1L, MailStatus.SENT, "발송 성공 메일");
        saveEmailLog(1L, MailStatus.FAILED, "발송 실패 메일1");
        saveEmailLog(2L, MailStatus.FAILED, "발송 실패 메일2");

        // FAILED 상태만 조건으로 목록을 조회한다.
        List<EmailLog> result = emailLogQueryMapper.findEmailLogByEmailStatus(MailStatus.FAILED);

        // 요청한 상태의 이메일 로그만 반환되는지 확인한다.
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(EmailLog::getEmailStatus)
                .containsOnly(MailStatus.FAILED);
    }
}
