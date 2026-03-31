package com.team2.activity.entity;

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
@Table(name = "activity_packages")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "package_title", nullable = false, length = 100)
    private String packageTitle;

    @Column(name = "package_description", columnDefinition = "TEXT")
    private String packageDescription;

    @Column(name = "po_id", length = 30)
    private String poId;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "package_id", nullable = false)
    private List<ActivityPackageViewer> viewers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "package_id", nullable = false)
    private List<ActivityPackageItem> items = new ArrayList<>();

    @Builder
    private ActivityPackage(String packageTitle, String packageDescription, String poId,
                             Long creatorId, List<ActivityPackageViewer> viewers,
                             List<ActivityPackageItem> items) {
        this.packageTitle = packageTitle;
        this.packageDescription = packageDescription;
        this.poId = poId;
        this.creatorId = creatorId;
        this.viewers = viewers != null ? new ArrayList<>(viewers) : new ArrayList<>();
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.createdAt = LocalDateTime.now(); // 단위 테스트(JPA 없는 환경)에서도 값이 있도록 설정
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

    public void update(String packageTitle, String packageDescription, String poId) {
        this.packageTitle = packageTitle;
        this.packageDescription = packageDescription;
        this.poId = poId;
    }
}
