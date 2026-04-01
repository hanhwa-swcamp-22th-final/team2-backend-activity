package com.team2.activity.command.domain.entity;

import com.team2.activity.command.domain.entity.converter.DocumentTypeConverter;
import com.team2.activity.command.domain.entity.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 이메일에 포함된 문서 유형 관계를 저장하는 보조 엔티티다.
@Entity
// 매핑 대상 테이블 이름을 지정한다.
@Table(name = "email_log_types")
// 읽기 전용 필드 접근을 위한 getter를 생성한다.
@Getter
// JPA 기본 생성자를 보호 수준으로 제공한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLogType {

    // 이메일 문서 유형 기본키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_type_id")
    private Long emailLogTypeId;

    // 문서 유형 enum을 DB 문자열과 변환해 저장한다.
    @Convert(converter = DocumentTypeConverter.class)
    @Column(name = "email_doc_type", nullable = false, length = 20)
    private DocumentType emailDocType;

    // 정적 팩터리 사용을 강제하기 위한 private 생성자다.
    private EmailLogType(DocumentType emailDocType) {
        // 전달받은 문서 유형을 엔티티 필드에 저장한다.
        this.emailDocType = emailDocType;
    }

    // 문서 유형만으로 보조 엔티티를 생성한다.
    public static EmailLogType of(DocumentType docType) {
        // private 생성자를 호출해 새 문서 유형 엔티티를 만든다.
        return new EmailLogType(docType);
    }
}
