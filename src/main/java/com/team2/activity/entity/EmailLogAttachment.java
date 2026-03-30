package com.team2.activity.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "email_log_attachments")
@Getter
public class EmailLogAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_attachment_id")
    private Long emailLogAttachmentId;

    @Column(name = "email_attachment_filename", nullable = false, length = 255)
    private String emailAttachmentFilename;

    protected EmailLogAttachment() {}

    private EmailLogAttachment(String emailAttachmentFilename) {
        this.emailAttachmentFilename = emailAttachmentFilename;
    }

    public static EmailLogAttachment of(String filename) {
        return new EmailLogAttachment(filename);
    }
}
