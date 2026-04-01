package com.team2.activity.command.application.dto;

import java.util.List;

// 활동 패키지 전체 수정 요청을 받는 DTO다.
public record ActivityPackageUpdateRequest(
        // 수정할 패키지 제목이다.
        String packageTitle,
        // 수정할 패키지 설명이다.
        String packageDescription,
        // 수정할 PO ID다.
        String poId,
        // 교체할 활동 ID 목록이다.
        List<Long> activityIds,
        // 교체할 viewer 사용자 ID 목록이다.
        List<Long> viewerIds
) {}
