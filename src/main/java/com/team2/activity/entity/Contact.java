package com.team2.activity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "writer_id")
    private Long writerId;

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "contact_position", length = 100)
    private String contactPosition;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_tel", length = 50)
    private String contactTel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Contact(Long contactId, Long clientId, Long writerId, String contactName, String contactPosition,
                    String contactEmail, String contactTel) {
        this.contactId = contactId;
        this.clientId = clientId;
        this.writerId = writerId;
        this.contactName = contactName;
        this.contactPosition = contactPosition;
        this.contactEmail = contactEmail;
        this.contactTel = contactTel;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String contactName, String contactPosition, String contactEmail, String contactTel) {
        this.contactName = contactName;
        this.contactPosition = contactPosition;
        this.contactEmail = contactEmail;
        this.contactTel = contactTel;
    }
}
