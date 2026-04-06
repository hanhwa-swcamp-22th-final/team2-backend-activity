package com.team2.activity.command.domain.entity;

import com.team2.activity.command.domain.entity.converter.MailStatusConverter;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 이메일 로그와 email_logs 테이블을 매핑하는 JPA 엔티티다.
@Entity
// 매핑 대상 테이블 이름을 지정한다.
@Table(name = "email_logs")
// 읽기 전용 필드 접근을 위한 getter를 생성한다.
@Getter
// JPA 기본 생성자를 보호 수준으로 제공한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLog {

    // 이메일 로그 기본키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_id")
    private Long emailLogId;

    // 거래처 ID다.
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    // 연관된 PO ID다.
    @Column(name = "po_id", length = 30)
    private String poId;

    // 이메일 제목이다.
    @Column(name = "email_title", nullable = false, length = 200)
    private String emailTitle;

    // 수신자 이름이다.
    @Column(name = "email_recipient_name", length = 100)
    private String emailRecipientName;

    // 수신자 이메일 주소다.
    @Column(name = "email_recipient_email", nullable = false, length = 255)
    private String emailRecipientEmail;

    // 발송자 사용자 ID다.
    @Column(name = "email_sender_id", nullable = false)
    private Long emailSenderId;

    // 발송 상태 enum을 DB 문자열과 변환해 저장한다.
    @Convert(converter = MailStatusConverter.class)
    @Column(name = "email_status", nullable = false, length = 10)
    private MailStatus emailStatus;

    // 실제 발송 시각이다.
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    // 생성 시각이다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 이메일에 포함된 문서 유형 목록이다.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "email_log_id", nullable = false)
    private List<EmailLogType> docTypes = new ArrayList<>();

    // 이메일 첨부파일 목록이다.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "email_log_id", nullable = false)
    private List<EmailLogAttachment> attachments = new ArrayList<>();

    // 빌더 기반 생성 로직을 제공한다.
    @Builder
    private EmailLog(Long emailLogId, Long clientId, String poId, String emailTitle, String emailRecipientName,
                     String emailRecipientEmail, Long emailSenderId, MailStatus emailStatus,
                     LocalDateTime emailSentAt, List<EmailLogType> docTypes, List<EmailLogAttachment> attachments) {
        // 빌더에서 받은 이메일 로그 ID를 엔티티 기본키 필드에 저장한다.
        this.emailLogId = emailLogId;
        // 빌더에서 받은 거래처 ID를 저장한다.
        this.clientId = clientId;
        // 빌더에서 받은 PO ID를 저장한다.
        this.poId = poId;
        // 빌더에서 받은 이메일 제목을 저장한다.
        this.emailTitle = emailTitle;
        // 빌더에서 받은 수신자 이름을 저장한다.
        this.emailRecipientName = emailRecipientName;
        // 빌더에서 받은 수신자 이메일을 저장한다.
        this.emailRecipientEmail = emailRecipientEmail;
        // 빌더에서 받은 발송자 ID를 저장한다.
        this.emailSenderId = emailSenderId;
        // 상태가 비어 있으면 기본값을 PENDING으로 보정한다.
        this.emailStatus = emailStatus != null ? emailStatus : MailStatus.PENDING;
        // 빌더에서 받은 발송 시각을 저장한다.
        this.emailSentAt = emailSentAt;
        // null 컬렉션을 빈 리스트로 보정해 NPE를 막는다.
        this.docTypes = docTypes != null ? new ArrayList<>(docTypes) : new ArrayList<>();
        // null 컬렉션을 빈 리스트로 보정해 NPE를 막는다.
        this.attachments = attachments != null ? new ArrayList<>(attachments) : new ArrayList<>();
    }

    // 최초 저장 직전에 생성 시각을 현재 시각으로 채운다.
    @PrePersist
    protected void onCreate() {
        // 최초 저장 시 생성 시각을 현재 시각으로 채운다.
        this.createdAt = LocalDateTime.now();
    }

    // 이메일 발송 상태를 새 값으로 갱신한다.
    public void updateStatus(MailStatus emailStatus) {
        // 새 메일 상태 값으로 필드를 교체한다.
        this.emailStatus = emailStatus;
    }

    // 이메일 발송 성공 시 상태를 SENT로 바꾸고 발송 시각을 기록한다.
    public void markAsSent() {
        // 발송 성공 상태로 전환한다.
        this.emailStatus = MailStatus.SENT;
        // 발송 완료 시각을 현재 시각으로 기록한다.
        this.emailSentAt = LocalDateTime.now();
    }

    // 이메일 발송 실패 시 상태를 FAILED로 바꾼다.
    public void markAsFailed() {
        // 발송 실패 상태로 전환한다.
        this.emailStatus = MailStatus.FAILED;
    }
}
