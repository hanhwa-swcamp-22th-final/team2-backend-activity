package com.team2.activity.command.domain.entity;

import com.team2.activity.command.domain.entity.enums.DocumentType;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EmailLog 엔티티 테스트")
class EmailLogTest {

    @Autowired
    private TestEntityManager em;

    private EmailLog buildBasicEmailLog() {
        return EmailLog.builder()
                .clientId(1L)
                .emailTitle("[PI] PI-2025-001 발송")
                .emailRecipientEmail("buyer@example.com")
                .emailRecipientName("John Doe")
                .emailSenderId(10L)
                .emailStatus(MailStatus.SENT)
                .emailSentAt(LocalDateTime.of(2025, 4, 10, 9, 0))
                .poId("PO-001")
                .docTypes(List.of(EmailLogType.of(DocumentType.PI), EmailLogType.of(DocumentType.CI)))
                .attachments(List.of(EmailLogAttachment.of("PI001.pdf"), EmailLogAttachment.of("CI001.pdf")))
                .build();
    }

    @Test
    @DisplayName("기본 EmailLog 생성 성공")
    void createEmailLog_basic() {
        EmailLog mail = buildBasicEmailLog();

        assertThat(mail.getClientId()).isEqualTo(1L);
        assertThat(mail.getEmailTitle()).isEqualTo("[PI] PI-2025-001 발송");
        assertThat(mail.getEmailRecipientEmail()).isEqualTo("buyer@example.com");
        assertThat(mail.getEmailRecipientName()).isEqualTo("John Doe");
        assertThat(mail.getEmailSenderId()).isEqualTo(10L);
        assertThat(mail.getEmailStatus()).isEqualTo(MailStatus.SENT);
        assertThat(mail.getEmailSentAt()).isEqualTo(LocalDateTime.of(2025, 4, 10, 9, 0));
        assertThat(mail.getPoId()).isEqualTo("PO-001");
        assertThat(mail.getDocTypes())
                .extracting(EmailLogType::getEmailDocType)
                .containsExactly(DocumentType.PI, DocumentType.CI);
        assertThat(mail.getAttachments())
                .extracting(EmailLogAttachment::getEmailAttachmentFilename)
                .containsExactly("PI001.pdf", "CI001.pdf");
    }

    @Test
    @DisplayName("status null 전달 시 기본값 PENDING 적용")
    void createEmailLog_defaultStatus_whenNull() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("테스트 메일")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailSentAt(LocalDateTime.now())
                .build();

        assertThat(mail.getEmailStatus()).isEqualTo(MailStatus.PENDING);
    }

    @Test
    @DisplayName("FAILED 상태로 EmailLog 생성")
    void createEmailLog_failedStatus() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("실패 메일")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailStatus(MailStatus.FAILED)
                .emailSentAt(LocalDateTime.now())
                .build();

        assertThat(mail.getEmailStatus()).isEqualTo(MailStatus.FAILED);
    }

    @Test
    @DisplayName("FAILED 상태 시 sentAt null 허용")
    void createEmailLog_failedStatus_sentAtNull() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("발송 실패 메일")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailStatus(MailStatus.FAILED)
                .build();

        assertThat(mail.getEmailStatus()).isEqualTo(MailStatus.FAILED);
        assertThat(mail.getEmailSentAt()).isNull();
    }

    @Test
    @DisplayName("updateStatus - SENT에서 FAILED로 변경")
    void updateStatus_sentToFailed() {
        EmailLog mail = buildBasicEmailLog();

        mail.updateStatus(MailStatus.FAILED);

        assertThat(mail.getEmailStatus()).isEqualTo(MailStatus.FAILED);
    }

    @Test
    @DisplayName("updateStatus - FAILED에서 SENT로 변경")
    void updateStatus_failedToSent() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("재발송 메일")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailStatus(MailStatus.FAILED)
                .emailSentAt(LocalDateTime.now())
                .build();

        mail.updateStatus(MailStatus.SENT);

        assertThat(mail.getEmailStatus()).isEqualTo(MailStatus.SENT);
    }

    @Test
    @DisplayName("docTypes null 전달 시 빈 리스트로 초기화")
    void createEmailLog_nullDocTypes() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("테스트")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailSentAt(LocalDateTime.now())
                .docTypes(null)
                .attachments(null)
                .build();

        assertThat(mail.getDocTypes()).isNotNull().isEmpty();
        assertThat(mail.getAttachments()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("복수 문서 유형 및 첨부파일 저장")
    void createEmailLog_multipleDocTypes() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("복합 문서 메일")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailSentAt(LocalDateTime.now())
                .docTypes(List.of(
                        EmailLogType.of(DocumentType.PI),
                        EmailLogType.of(DocumentType.CI),
                        EmailLogType.of(DocumentType.PL)))
                .attachments(List.of(
                        EmailLogAttachment.of("PI001.pdf"),
                        EmailLogAttachment.of("CI001.pdf"),
                        EmailLogAttachment.of("PL001.pdf")))
                .build();

        assertThat(mail.getDocTypes())
                .extracting(EmailLogType::getEmailDocType)
                .containsExactlyInAnyOrder(DocumentType.PI, DocumentType.CI, DocumentType.PL);
        assertThat(mail.getAttachments()).hasSize(3);
    }

    @Test
    @DisplayName("영문 문서 유형(PRODUCTION_ORDER, SHIPMENT_ORDER) 저장")
    void createEmailLog_koreanDocTypes() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("내부 지시서 메일")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailSentAt(LocalDateTime.now())
                .docTypes(List.of(
                        EmailLogType.of(DocumentType.PRODUCTION_ORDER),
                        EmailLogType.of(DocumentType.SHIPMENT_ORDER)))
                .build();

        assertThat(mail.getDocTypes())
                .extracting(EmailLogType::getEmailDocType)
                .containsExactlyInAnyOrder(DocumentType.PRODUCTION_ORDER, DocumentType.SHIPMENT_ORDER);
    }

    @Test
    @DisplayName("MailStatus 열거값 확인")
    void mailStatus_values() {
        assertThat(MailStatus.values())
                .containsExactlyInAnyOrder(
                        MailStatus.PENDING,
                        MailStatus.SENDING,
                        MailStatus.SENT,
                        MailStatus.FAILED
                );
    }

    @Test
    @DisplayName("MailStatus JSON 직렬화 - 영문 displayName 반환")
    void mailStatus_displayName() {
        assertThat(MailStatus.PENDING.getDisplayName()).isEqualTo("pending"); // 발송 전 초기 상태
        assertThat(MailStatus.SENT.getDisplayName()).isEqualTo("sent");      // 발송 성공
        assertThat(MailStatus.FAILED.getDisplayName()).isEqualTo("failed");  // 발송 실패
    }

    @Test
    @DisplayName("MailStatus JSON 역직렬화 - 영문 문자열로 생성")
    void mailStatus_fromDisplayName() {
        assertThat(MailStatus.from("pending")).isEqualTo(MailStatus.PENDING); // 발송 전 초기 상태
        assertThat(MailStatus.from("sent")).isEqualTo(MailStatus.SENT);      // 발송 성공
        assertThat(MailStatus.from("failed")).isEqualTo(MailStatus.FAILED);  // 발송 실패
    }

    @Test
    @DisplayName("DocumentType 열거값 확인")
    void documentType_values() {
        assertThat(DocumentType.values())
                .containsExactlyInAnyOrder(
                        DocumentType.PI,
                        DocumentType.CI,
                        DocumentType.PL,
                        DocumentType.PRODUCTION_ORDER,
                        DocumentType.SHIPMENT_ORDER
                );
    }

    @Test
    @DisplayName("DocumentType JSON 직렬화 - displayName 반환")
    void documentType_displayName() {
        assertThat(DocumentType.PI.getDisplayName()).isEqualTo("PI");
        assertThat(DocumentType.CI.getDisplayName()).isEqualTo("CI");
        assertThat(DocumentType.PL.getDisplayName()).isEqualTo("PL");
        assertThat(DocumentType.PRODUCTION_ORDER.getDisplayName()).isEqualTo("production_order");
        assertThat(DocumentType.SHIPMENT_ORDER.getDisplayName()).isEqualTo("shipment_order");
    }

    @Test
    @DisplayName("DocumentType JSON 역직렬화 - 문자열로 생성")
    void documentType_fromDisplayName() {
        assertThat(DocumentType.from("PI")).isEqualTo(DocumentType.PI);
        assertThat(DocumentType.from("CI")).isEqualTo(DocumentType.CI);
        assertThat(DocumentType.from("PL")).isEqualTo(DocumentType.PL);
        assertThat(DocumentType.from("production_order")).isEqualTo(DocumentType.PRODUCTION_ORDER);
        assertThat(DocumentType.from("shipment_order")).isEqualTo(DocumentType.SHIPMENT_ORDER);
    }

    @Test
    @DisplayName("PO 없는 EmailLog 생성")
    void createEmailLog_withoutPoId() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("PO 없는 메일")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailSentAt(LocalDateTime.now())
                .build();

        assertThat(mail.getPoId()).isNull();
    }

    @Test
    @DisplayName("MailStatus.from() - 잘못된 값 전달 시 예외 발생")
    void mailStatus_fromInvalidValue_throwsException() {
        assertThatThrownBy(() -> MailStatus.from("잘못된값"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("DocumentType.from() - 잘못된 값 전달 시 예외 발생")
    void documentType_fromInvalidValue_throwsException() {
        assertThatThrownBy(() -> DocumentType.from("잘못된값"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("DB - 기본 EmailLog 저장 및 조회")
    void db_saveAndFind() {
        EmailLog mail = buildBasicEmailLog();

        EmailLog saved = em.persistFlushFind(mail);

        assertThat(saved.getEmailLogId()).isNotNull();
        assertThat(saved.getEmailTitle()).isEqualTo("[PI] PI-2025-001 발송");
        assertThat(saved.getEmailStatus()).isEqualTo(MailStatus.SENT);
        assertThat(saved.getClientId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DB - docTypes, attachments cascade 저장 확인")
    void db_saveWithDocTypesAndAttachments() {
        EmailLog mail = buildBasicEmailLog();

        EmailLog saved = em.persistFlushFind(mail);

        assertThat(saved.getDocTypes())
                .extracting(EmailLogType::getEmailDocType)
                .containsExactlyInAnyOrder(DocumentType.PI, DocumentType.CI);
        assertThat(saved.getAttachments())
                .extracting(EmailLogAttachment::getEmailAttachmentFilename)
                .containsExactlyInAnyOrder("PI001.pdf", "CI001.pdf");
    }

    @Test
    @DisplayName("DB - MailStatus/DocumentType Enum 컨버터 저장 확인")
    void db_enumConverters() {
        EmailLog mail = EmailLog.builder()
                .clientId(1L)
                .emailTitle("실패 메일")
                .emailRecipientEmail("test@example.com")
                .emailSenderId(10L)
                .emailStatus(MailStatus.FAILED)
                .docTypes(List.of(EmailLogType.of(DocumentType.PRODUCTION_ORDER)))
                .build();

        EmailLog saved = em.persistFlushFind(mail);

        assertThat(saved.getEmailStatus()).isEqualTo(MailStatus.FAILED);
        assertThat(saved.getDocTypes().get(0).getEmailDocType()).isEqualTo(DocumentType.PRODUCTION_ORDER);
    }

    @Test
    @DisplayName("DB - updateStatus() 후 변경사항 DB 반영 확인")
    void db_updateStatusPersists() {
        EmailLog mail = buildBasicEmailLog();
        EmailLog saved = em.persistAndFlush(mail);

        saved.updateStatus(MailStatus.FAILED);
        em.flush();
        em.clear();

        EmailLog found = em.find(EmailLog.class, saved.getEmailLogId());
        assertThat(found.getEmailStatus()).isEqualTo(MailStatus.FAILED);
    }
}
