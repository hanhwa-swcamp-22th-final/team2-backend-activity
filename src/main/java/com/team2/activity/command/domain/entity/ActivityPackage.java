package com.team2.activity.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 활동 패키지와 activity_packages 테이블을 매핑하는 JPA 엔티티다.
@Entity
// 매핑 대상 테이블 이름을 지정한다.
@Table(name = "activity_packages")
// 읽기 전용 필드 접근을 위한 getter를 생성한다.
@Getter
// JPA 기본 생성자를 보호 수준으로 제공한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityPackage {

    // 패키지 기본키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long packageId;

    // 패키지 제목이다.
    @Column(name = "package_title", nullable = false, length = 100)
    private String packageTitle;

    // 패키지 설명이다.
    @Column(name = "package_description", columnDefinition = "TEXT")
    private String packageDescription;

    // 연관된 PO ID다.
    @Column(name = "po_id", length = 30)
    private String poId;

    // 패키지 생성자 ID다.
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    // 생성 시각이다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 마지막 수정 시각이다.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 패키지를 열람할 수 있는 사용자 목록이다.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "package_id", nullable = false)
    private List<ActivityPackageViewer> viewers = new ArrayList<>();

    // 패키지에 포함된 활동 목록이다.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "package_id", nullable = false)
    private List<ActivityPackageItem> items = new ArrayList<>();

    // 빌더 기반 생성 로직을 제공한다.
    @Builder
    private ActivityPackage(Long packageId, String packageTitle, String packageDescription, String poId,
                             Long creatorId, List<ActivityPackageViewer> viewers,
                             List<ActivityPackageItem> items) {
        // 빌더에서 받은 패키지 ID를 엔티티 기본키 필드에 저장한다.
        this.packageId = packageId;
        // 빌더에서 받은 제목을 저장한다.
        this.packageTitle = packageTitle;
        // 빌더에서 받은 설명을 저장한다.
        this.packageDescription = packageDescription;
        // 빌더에서 받은 PO ID를 저장한다.
        this.poId = poId;
        // 빌더에서 받은 생성자 ID를 저장한다.
        this.creatorId = creatorId;
        // null이 아니면 새 ArrayList로 복사해 viewers 컬렉션을 초기화한다.
        this.viewers = viewers != null ? new ArrayList<>(viewers) : new ArrayList<>();
        // null이 아니면 새 ArrayList로 복사해 items 컬렉션을 초기화한다.
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
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

    // 수정 가능한 기본 정보를 한 번에 갱신한다.
    public void update(String packageTitle, String packageDescription, String poId) {
        // 새 제목으로 값을 교체한다.
        this.packageTitle = packageTitle;
        // 새 설명으로 값을 교체한다.
        this.packageDescription = packageDescription;
        // 새 PO ID로 값을 교체한다.
        this.poId = poId;
    }
}
