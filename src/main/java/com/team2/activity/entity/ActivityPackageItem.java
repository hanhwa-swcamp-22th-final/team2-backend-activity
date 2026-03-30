package com.team2.activity.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "activity_package_items")
@Getter
public class ActivityPackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_item_id")
    private Long packageItemId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    protected ActivityPackageItem() {}

    private ActivityPackageItem(Long activityId) {
        this.activityId = activityId;
    }

    public static ActivityPackageItem of(Long activityId) {
        return new ActivityPackageItem(activityId);
    }
}
