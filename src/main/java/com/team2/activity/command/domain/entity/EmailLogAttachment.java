package com.team2.activity.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 이메일 첨부파일 메타데이터를 저장하는 보조 엔티티다.
@Entity
@Table(name = "email_log_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLogAttachment {

    // 첨부파일 기본키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_attachment_id")
    private Long emailLogAttachmentId;

    // 첨부파일 원본 파일명이다.
    @Column(name = "email_attachment_filename", nullable = false, length = 255)
    private String emailAttachmentFilename;

    // 첨부파일의 저장 경로다.
    @Column(name = "email_attachment_file_path", length = 500)
    private String filePath;

    // 정적 팩터리 사용을 강제하기 위한 private 생성자다.
    private EmailLogAttachment(String emailAttachmentFilename) {
        this.emailAttachmentFilename = emailAttachmentFilename;
    }

    // 파일 경로를 포함한 정적 팩터리용 private 생성자다.
    private EmailLogAttachment(String emailAttachmentFilename, String filePath) {
        this.emailAttachmentFilename = emailAttachmentFilename;
        this.filePath = filePath;
    }

    // 파일명만으로 첨부파일 엔티티를 생성한다.
    public static EmailLogAttachment of(String filename) {
        return new EmailLogAttachment(filename);
    }

    // 파일명과 파일 경로로 첨부파일 엔티티를 생성한다.
    public static EmailLogAttachment of(String filename, String filePath) {
        return new EmailLogAttachment(filename, filePath);
    }
}
