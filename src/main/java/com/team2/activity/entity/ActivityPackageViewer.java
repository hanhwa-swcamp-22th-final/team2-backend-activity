package com.team2.activity.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "activity_package_viewers")
@Getter
public class ActivityPackageViewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_viewer_id")
    private Long packageViewerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    protected ActivityPackageViewer() {}

    private ActivityPackageViewer(Long userId) {
        this.userId = userId;
    }

    public static ActivityPackageViewer of(Long userId) {
        return new ActivityPackageViewer(userId);
    }
}
