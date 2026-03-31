package com.team2.activity.query.repository;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.EmailLogAttachment;
import com.team2.activity.entity.EmailLogType;
import com.team2.activity.entity.enums.DocumentType;
import com.team2.activity.entity.enums.MailStatus;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@DisplayName("EmailLogQueryRepository 테스트")
class EmailLogQueryRepositoryTest {

    @Autowired
    private EmailLogQueryMapper emailLogQueryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private EmailLog saveEmailLog(Long clientId, MailStatus status, String title) {
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

        Long emailLogId = jdbcTemplate.queryForObject("SELECT MAX(email_log_id) FROM email_logs", Long.class);

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

        return emailLogQueryMapper.findById(emailLogId);
    }

    @Test
    @DisplayName("ID로 이메일 로그 조회 시 문서 유형과 첨부파일이 매핑된다")
    void findById_mapsEmailLogFieldsAndCollections() {
        EmailLog saved = saveEmailLog(1L, MailStatus.SENT, "견적 메일");

        EmailLog found = emailLogQueryMapper.findById(saved.getEmailLogId());

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
        saveEmailLog(1L, MailStatus.SENT, "고객1 메일1");
        saveEmailLog(1L, MailStatus.FAILED, "고객1 메일2");
        saveEmailLog(2L, MailStatus.SENT, "고객2 메일");

        List<EmailLog> result = emailLogQueryMapper.findByClientId(1L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(EmailLog::getClientId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("상태로 이메일 로그 목록을 조회한다")
    void findByEmailStatus_returnsOnlyMatchedEmailLogs() {
        saveEmailLog(1L, MailStatus.SENT, "발송 성공 메일");
        saveEmailLog(1L, MailStatus.FAILED, "발송 실패 메일1");
        saveEmailLog(2L, MailStatus.FAILED, "발송 실패 메일2");

        List<EmailLog> result = emailLogQueryMapper.findByEmailStatus(MailStatus.FAILED);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(EmailLog::getEmailStatus)
                .containsOnly(MailStatus.FAILED);
    }
}
