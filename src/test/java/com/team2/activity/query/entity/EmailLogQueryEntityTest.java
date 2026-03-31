package com.team2.activity.query.entity;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.EmailLogAttachment;
import com.team2.activity.entity.EmailLogType;
import com.team2.activity.entity.enums.DocumentType;
import com.team2.activity.entity.enums.MailStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Query EmailLog 엔티티 테스트")
class EmailLogQueryEntityTest {

    @Test
    @DisplayName("조회용 EmailLog는 문서 유형과 첨부파일을 가진다")
    void createReadEmailLog_withCollections() {
        EmailLog emailLog = EmailLog.builder()
                .clientId(1L)
                .poId("PO-001")
                .emailTitle("안내 메일")
                .emailRecipientName("고객")
                .emailRecipientEmail("client@example.com")
                .emailSenderId(10L)
                .emailStatus(MailStatus.SENT)
                .emailSentAt(LocalDateTime.of(2025, 4, 1, 9, 0))
                .docTypes(List.of(EmailLogType.of(DocumentType.PI), EmailLogType.of(DocumentType.CI)))
                .attachments(List.of(EmailLogAttachment.of("PI001.pdf"), EmailLogAttachment.of("CI001.pdf")))
                .build();

        assertThat(emailLog.getEmailStatus()).isEqualTo(MailStatus.SENT);
        assertThat(emailLog.getDocTypes())
                .extracting(EmailLogType::getEmailDocType)
                .containsExactly(DocumentType.PI, DocumentType.CI);
        assertThat(emailLog.getAttachments())
                .extracting(EmailLogAttachment::getEmailAttachmentFilename)
                .containsExactly("PI001.pdf", "CI001.pdf");
    }

    @Test
    @DisplayName("조회용 EmailLog는 상태 미지정 시 SENT 기본값을 가진다")
    void createReadEmailLog_defaultStatus() {
        EmailLog emailLog = EmailLog.builder()
                .clientId(1L)
                .emailTitle("기본 상태 메일")
                .emailRecipientEmail("client@example.com")
                .emailSenderId(10L)
                .build();

        assertThat(emailLog.getEmailStatus()).isEqualTo(MailStatus.SENT);
    }
}
