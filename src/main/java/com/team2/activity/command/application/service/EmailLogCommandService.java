package com.team2.activity.command.application.service;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.command.domain.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 이메일 로그 쓰기 유스케이스를 담당하는 command service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 쓰기 작업이 하나의 트랜잭션으로 처리되도록 보장한다.
@Transactional
public class EmailLogCommandService {

    // 이메일 로그 저장소 접근을 담당한다.
    private final EmailLogRepository emailLogRepository;

    // 실제 이메일을 발송하는 JavaMail 구현체를 주입한다.
    private final JavaMailSender mailSender;

    // 발송자 이메일 주소를 환경 설정에서 읽어 온다. 미설정 시 빈 문자열을 사용한다.
    @Value("${spring.mail.username:}")
    private String senderEmail;

    // 새 이메일 로그를 저장하고 실제 메일 발송을 시도한다.
    public EmailLog createEmailLog(EmailLog emailLog) {
        // 초기 상태(FAILED)인 이메일 로그를 저장소에 먼저 저장한다.
        EmailLog saved = emailLogRepository.save(emailLog);
        // 저장 후 실제 이메일 발송을 시도하고 성공 시 상태를 갱신한다.
        sendMail(saved);
        // 발송 결과가 반영된 엔티티를 반환한다.
        return saved;
    }

    // 실패 상태의 이메일 로그를 재전송해 상태를 SENT로 되돌린다.
    public EmailLog resend(Long emailLogId) {
        // 재전송 대상 이메일 로그를 먼저 조회한다.
        EmailLog emailLog = findById(emailLogId);
        // 이미 발송 성공 상태면 중복 재전송을 막는다.
        if (emailLog.getEmailStatus() == MailStatus.SENT) {
            throw new IllegalStateException("이미 발송된 이메일입니다.");
        }
        // 실제 이메일 발송을 시도하고 성공 시 상태를 SENT로 갱신한다.
        sendMail(emailLog);
        // 재전송 결과가 반영된 엔티티를 그대로 반환한다.
        return emailLog;
    }

    // 이메일 로그를 조회한 뒤 삭제한다.
    public void deleteEmailLog(Long emailLogId) {
        // 삭제 대상 이메일 로그를 먼저 조회한다.
        EmailLog emailLog = findById(emailLogId);
        // 조회한 이메일 로그를 저장소에서 삭제한다.
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
            // 발송 실패 시 상태를 변경하지 않고 FAILED 상태를 유지한다.
        }
    }

    // ID로 이메일 로그를 조회하고 없으면 예외를 던진다.
    private EmailLog findById(Long emailLogId) {
        return emailLogRepository.findById(emailLogId)
                // 조회 결과가 없으면 이메일 로그 없음 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));
    }
}
