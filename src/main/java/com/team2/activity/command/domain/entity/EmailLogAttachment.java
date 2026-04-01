package com.team2.activity.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 이메일 첨부파일 메타데이터를 저장하는 보조 엔티티다.
@Entity
// 매핑 대상 테이블 이름을 지정한다.
@Table(name = "email_log_attachments")
// 필드 조회용 getter를 생성한다.
@Getter
// 테스트나 매핑 편의를 위한 setter를 허용한다.
@Setter
// JPA 기본 생성자를 보호 수준으로 제공한다.
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

    // 정적 팩터리 사용을 강제하기 위한 private 생성자다.
    private EmailLogAttachment(String emailAttachmentFilename) {
        // 전달받은 파일명을 엔티티 필드에 저장한다.
        this.emailAttachmentFilename = emailAttachmentFilename;
    }

    // 파일명만으로 첨부파일 엔티티를 생성한다.
    public static EmailLogAttachment of(String filename) {
        // private 생성자를 호출해 새 첨부파일 엔티티를 만든다.
        return new EmailLogAttachment(filename);
    }
}
