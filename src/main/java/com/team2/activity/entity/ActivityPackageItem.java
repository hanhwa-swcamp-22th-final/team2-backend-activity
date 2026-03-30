package com.team2.activity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activity_package_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityPackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_item_id")
    private Long packageItemId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    private ActivityPackageItem(Long activityId) {
        this.activityId = activityId;
    }

    public static ActivityPackageItem of(Long activityId) {
        return new ActivityPackageItem(activityId);
    }
}
