package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.ActivityPackageViewer;

import java.util.List;

// 활동 패키지 조회/응답에 사용하는 DTO다.
public record ActivityPackageResponse(
        // 패키지 ID다.
        Long packageId,
        // 패키지 제목이다.
        String packageTitle,
        // 패키지 설명이다.
        String packageDescription,
        // 연관된 PO ID다.
        String poId,
        // 패키지 생성자 ID다.
        Long creatorId,
        // 포함된 활동 ID 목록이다.
        List<Long> activityIds,
        // 열람 가능한 사용자 ID 목록이다.
        List<Long> viewerIds
) {
    // 엔티티를 API 응답 전용 DTO로 변환한다.
    public static ActivityPackageResponse from(ActivityPackage p) {
        return new ActivityPackageResponse(
                // 엔티티의 패키지 ID를 응답에 담는다.
                p.getPackageId(),
                // 엔티티의 패키지 제목을 응답에 담는다.
                p.getPackageTitle(),
                // 엔티티의 패키지 설명을 응답에 담는다.
                p.getPackageDescription(),
                // 엔티티의 PO ID를 응답에 담는다.
                p.getPoId(),
                // 엔티티의 생성자 ID를 응답에 담는다.
                p.getCreatorId(),
                // item 엔티티 목록을 activity ID 목록으로 평탄화한다.
                p.getItems().stream().map(ActivityPackageItem::getActivityId).toList(),
                // viewer 엔티티 목록을 사용자 ID 목록으로 평탄화한다.
                p.getViewers().stream().map(ActivityPackageViewer::getUserId).toList()
        );
    }
}
