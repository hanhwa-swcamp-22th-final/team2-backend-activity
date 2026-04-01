package com.team2.activity.entity;

import com.team2.activity.entity.converter.MailStatusConverter;
import com.team2.activity.entity.enums.MailStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_id")
    private Long emailLogId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "po_id", length = 30)
    private String poId;

    @Column(name = "email_title", nullable = false, length = 200)
    private String emailTitle;

    @Column(name = "email_recipient_name", length = 100)
    private String emailRecipientName;

    @Column(name = "email_recipient_email", nullable = false, length = 255)
    private String emailRecipientEmail;

    @Column(name = "email_sender_id", nullable = false)
    private Long emailSenderId;

    @Convert(converter = MailStatusConverter.class)
    @Column(name = "email_status", nullable = false, length = 10)
    private MailStatus emailStatus;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "email_log_id", nullable = false)
    private List<EmailLogType> docTypes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "email_log_id", nullable = false)
    private List<EmailLogAttachment> attachments = new ArrayList<>();

    @Builder
    private EmailLog(Long emailLogId, Long clientId, String poId, String emailTitle, String emailRecipientName,
                     String emailRecipientEmail, Long emailSenderId, MailStatus emailStatus,
                     LocalDateTime emailSentAt, List<EmailLogType> docTypes, List<EmailLogAttachment> attachments) {
        this.emailLogId = emailLogId;
        this.clientId = clientId;
        this.poId = poId;
        this.emailTitle = emailTitle;
        this.emailRecipientName = emailRecipientName;
        this.emailRecipientEmail = emailRecipientEmail;
        this.emailSenderId = emailSenderId;
        this.emailStatus = emailStatus != null ? emailStatus : MailStatus.SENT;
        this.emailSentAt = emailSentAt;
        this.docTypes = docTypes != null ? new ArrayList<>(docTypes) : new ArrayList<>();
        this.attachments = attachments != null ? new ArrayList<>(attachments) : new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(MailStatus emailStatus) {
        this.emailStatus = emailStatus;
    }
}
