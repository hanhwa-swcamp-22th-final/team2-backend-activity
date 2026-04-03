package com.team2.activity.command.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    // 패키지 기간 시작일이다.
    @Column(name = "date_from")
    private LocalDate dateFrom;

    // 패키지 기간 종료일이다.
    @Column(name = "date_to")
    private LocalDate dateTo;

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
                             Long creatorId, LocalDate dateFrom, LocalDate dateTo,
                             List<ActivityPackageViewer> viewers, List<ActivityPackageItem> items) {
        this.packageId = packageId;
        this.packageTitle = packageTitle;
        this.packageDescription = packageDescription;
        this.poId = poId;
        this.creatorId = creatorId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.viewers = viewers != null ? new ArrayList<>(viewers) : new ArrayList<>();
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
    public void update(String packageTitle, String packageDescription, String poId,
                       LocalDate dateFrom, LocalDate dateTo) {
        this.packageTitle = packageTitle;
        this.packageDescription = packageDescription;
        this.poId = poId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}
