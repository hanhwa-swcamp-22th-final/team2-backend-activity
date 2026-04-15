package com.team2.activity.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 연락처와 contacts 테이블을 매핑하는 JPA 엔티티다.
@Entity
// 매핑 대상 테이블 이름을 지정한다.
@Table(name = "contacts")
// 읽기 전용 필드 접근을 위한 getter를 생성한다.
@Getter
// JPA 기본 생성자를 보호 수준으로 제공한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contact {

    // 연락처 기본키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Long contactId;

    // 거래처 ID다.
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    // 연락처 작성자 ID. 컨택리스트는 작성자 개인의 인맥 자산이므로 조회 시 이 값으로 필터.
    // Buyer sync 시 같은 팀 영업담당자 각각에 대해 별도 Contact row 가 생성되어 (writerId 가
    // 각 사용자) 결과적으로 팀이 동일 buyer 정보를 자동으로 받게 된다.
    @Column(name = "writer_id")
    private Long writerId;

    // 연락처 이름이다.
    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    // 연락처 직책이다.
    @Column(name = "contact_position", length = 100)
    private String contactPosition;

    // 연락처 이메일이다.
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    // 연락처 전화번호다.
    @Column(name = "contact_tel", length = 50)
    private String contactTel;

    // 생성 시각이다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 마지막 수정 시각이다.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 빌더 기반 생성 로직을 제공한다.
    @Builder
    private Contact(Long contactId, Long clientId, Long writerId,
                    String contactName, String contactPosition, String contactEmail, String contactTel) {
        this.contactId = contactId;
        this.clientId = clientId;
        this.writerId = writerId;
        this.contactName = contactName;
        this.contactPosition = contactPosition;
        this.contactEmail = contactEmail;
        this.contactTel = contactTel;
    }

    // 최초 저장 직전에 생성/수정 시각을 현재 시각으로 채운다.
    @PrePersist
    protected void onCreate() {
        // 최초 저장 시 생성 시각을 현재 시각으로 채운다.
        this.createdAt = LocalDateTime.now();
        // 최초 저장 시 수정 시각도 현재 시각으로 맞춘다.
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 직전에 수정 시각을 현재 시각으로 갱신한다.
    @PreUpdate
    protected void onUpdate() {
        // 수정 직전에 수정 시각을 현재 시각으로 갱신한다.
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 가능한 연락처 필드를 한 번에 갱신한다.
    public void update(String contactName, String contactPosition, String contactEmail, String contactTel) {
        // 새 이름으로 값을 교체한다.
        this.contactName = contactName;
        // 새 직책으로 값을 교체한다.
        this.contactPosition = contactPosition;
        // 새 이메일로 값을 교체한다.
        this.contactEmail = contactEmail;
        // 새 전화번호로 값을 교체한다.
        this.contactTel = contactTel;
    }
}
