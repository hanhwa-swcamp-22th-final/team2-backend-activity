package com.team2.activity.command.application.service;

import com.team2.activity.command.application.dto.EmailLogInternalRequest;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.EmailLogAttachment;
import com.team2.activity.command.domain.entity.EmailLogType;
import com.team2.activity.command.domain.entity.enums.DocumentType;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.command.domain.repository.EmailLogRepository;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// 이메일 로그 쓰기 유스케이스를 담당하는 command service다.
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailLogCommandService {

    private final EmailLogRepository emailLogRepository;
    private final JavaMailSender mailSender;
    private final DocumentsFeignClient documentsFeignClient;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Transactional
    public EmailLog createEmailLog(EmailLog emailLog) {
        return emailLogRepository.save(emailLog);
    }

    public void attemptSend(EmailLog emailLog) {
        sendMail(emailLog);
        if (emailLog.getEmailStatus() == MailStatus.SENT) {
            try {
                emailLogRepository.save(emailLog);
            } catch (Exception e) {
                log.error("메일 발송 성공했으나 DB 상태 업데이트 실패 [emailLogId={}]: {}", emailLog.getEmailLogId(), e.getMessage(), e);
            }
        }
    }

    public EmailLog resend(Long emailLogId) {
        EmailLog emailLog = findById(emailLogId);
        if (emailLog.getEmailStatus() == MailStatus.SENT) {
            throw new IllegalStateException("이미 발송된 이메일입니다.");
        }
        if (emailLog.getEmailStatus() == MailStatus.PENDING) {
            throw new IllegalStateException("아직 발송 시도 전인 이메일입니다.");
        }
        sendMailWithAttachments(emailLog);
        if (emailLog.getEmailStatus() == MailStatus.FAILED) {
            throw new IllegalStateException("이메일 재전송에 실패했습니다.");
        }
        try {
            emailLogRepository.save(emailLog);
        } catch (Exception e) {
            log.error("메일 재전송 성공했으나 DB 상태 업데이트 실패 [emailLogId={}]: {}", emailLog.getEmailLogId(), e.getMessage(), e);
        }
        return emailLog;
    }

    @Transactional
    public void deleteEmailLog(Long emailLogId) {
        EmailLog emailLog = findById(emailLogId);
        emailLogRepository.delete(emailLog);
    }

    // Documents 서비스에서 호출하여 이메일 로그를 내부적으로 생성한다.
    @Transactional
    public void createEmailLogFromInternal(EmailLogInternalRequest request) {
        // 문서 유형 목록을 변환한다.
        List<EmailLogType> docTypeList = request.docTypes() != null
                ? request.docTypes().stream()
                    .map(dt -> EmailLogType.of(DocumentType.from(dt)))
                    .toList()
                : List.of();

        // S3 키를 포함한 첨부파일 목록을 구성한다.
        List<EmailLogAttachment> attachments = new ArrayList<>();
        if (request.attachmentFilenames() != null && request.s3Keys() != null) {
            for (int i = 0; i < request.attachmentFilenames().size(); i++) {
                String filename = request.attachmentFilenames().get(i);
                String s3Key = i < request.s3Keys().size() ? request.s3Keys().get(i) : null;
                attachments.add(EmailLogAttachment.of(filename, s3Key));
            }
        }

        // 발송 상태를 결정한다.
        MailStatus status = "SENT".equals(request.emailStatus()) ? MailStatus.SENT : MailStatus.FAILED;

        EmailLog emailLog = EmailLog.builder()
                .clientId(request.clientId())
                .poId(request.poId())
                .emailTitle(request.emailTitle())
                .emailRecipientName(request.emailRecipientName())
                .emailRecipientEmail(request.emailRecipientEmail())
                .emailSenderId(request.emailSenderId())
                .emailStatus(status)
                .docTypes(docTypeList)
                .attachments(attachments)
                .build();

        if (status == MailStatus.SENT) {
            emailLog.markAsSent();
        }

        emailLogRepository.save(emailLog);
    }

    // 첨부파일을 포함한 MimeMessage로 이메일을 발송한다.
    private void sendMailWithAttachments(EmailLog emailLog) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(emailLog.getEmailRecipientEmail());
            helper.setSubject(emailLog.getEmailTitle());
            helper.setText(emailLog.getEmailRecipientName() + " 님께 보내는 메일입니다.");

            // 첨부파일을 Documents 서비스에서 다운로드하여 첨부한다.
            if (emailLog.getAttachments() != null) {
                for (EmailLogAttachment attachment : emailLog.getAttachments()) {
                    if (attachment.getS3Key() != null) {
                        try {
                            byte[] pdfBytes = documentsFeignClient.downloadPdf(attachment.getS3Key());
                            helper.addAttachment(
                                    attachment.getEmailAttachmentFilename(),
                                    new ByteArrayResource(pdfBytes),
                                    "application/pdf"
                            );
                        } catch (Exception e) {
                            log.error("첨부파일 다운로드 실패 [s3Key={}]: {}", attachment.getS3Key(), e.getMessage());
                        }
                    }
                }
            }

            mailSender.send(mimeMessage);
            emailLog.markAsSent();
        } catch (Exception e) {
            emailLog.markAsFailed();
            log.error("이메일 발송 실패 [emailLogId={}, to={}]: {}",
                    emailLog.getEmailLogId(), emailLog.getEmailRecipientEmail(), e.getMessage(), e);
        }
    }

    // 이메일을 실제로 발송하고 성공 시 엔티티 상태를 SENT로 갱신한다.
    private void sendMail(EmailLog emailLog) {
        try {
            // 발송할 단순 텍스트 메일 메시지 객체를 생성한다.
            SimpleMailMessage message = new SimpleMailMessage();
            // 발송자 주소를 환경 설정 값으로 지정한다.
            message.setFrom(senderEmail);
            // 수신자 이메일 주소를 엔티티에서 가져와 설정한다.
            message.setTo(emailLog.getEmailRecipientEmail());
            // 이메일 제목을 엔티티 값으로 설정한다.
            message.setSubject(emailLog.getEmailTitle());
            // 수신자 이름을 본문에 포함한 간단한 텍스트를 작성한다.
            message.setText(emailLog.getEmailRecipientName() + " 님께 보내는 메일입니다.");
            // 설정한 메시지를 SMTP 서버로 전송한다.
            mailSender.send(message);
            // 발송 성공 시 상태를 SENT로 바꾸고 발송 시각을 기록한다.
            emailLog.markAsSent();
        } catch (Exception e) {
            // 발송 실패 시 상태를 FAILED로 명시적으로 전환한다.
            emailLog.markAsFailed();
            log.error("이메일 발송 실패 [emailLogId={}, to={}]: {}", emailLog.getEmailLogId(), emailLog.getEmailRecipientEmail(), e.getMessage(), e);
        }
    }

    // ID로 이메일 로그를 조회하고 없으면 예외를 던진다.
    private EmailLog findById(Long emailLogId) {
        return emailLogRepository.findById(emailLogId)
                // 조회 결과가 없으면 이메일 로그 없음 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));
    }
}
