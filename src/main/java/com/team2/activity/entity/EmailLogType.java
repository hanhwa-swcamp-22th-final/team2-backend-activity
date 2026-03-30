package com.team2.activity.entity;

import com.team2.activity.entity.converter.DocumentTypeConverter;
import com.team2.activity.entity.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_log_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailLogType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_type_id")
    private Long emailLogTypeId;

    @Convert(converter = DocumentTypeConverter.class)
    @Column(name = "email_doc_type", nullable = false, length = 20)
    private DocumentType emailDocType;

    private EmailLogType(DocumentType emailDocType) {
        this.emailDocType = emailDocType;
    }

    public static EmailLogType of(DocumentType docType) {
        return new EmailLogType(docType);
    }
}
