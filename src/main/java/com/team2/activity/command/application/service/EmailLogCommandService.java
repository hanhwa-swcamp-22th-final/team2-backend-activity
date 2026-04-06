package com.team2.activity.command.application.service;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.command.domain.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 이메일 로그 쓰기 유스케이스를 담당하는 command service다.
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailLogCommandService {

    private final EmailLogRepository emailLogRepository;
    private final JavaMailSender mailSender;

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
        sendMail(emailLog);
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
