package com.team2.activity.dto;

import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.entity.ActivityPackageItem;
import com.team2.activity.entity.ActivityPackageViewer;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

// 활동 패키지 생성 요청 본문을 받는 DTO다.
public record ActivityPackageCreateRequest(
        // 패키지 제목이다.
        @NotBlank String packageTitle,
        // 패키지 설명이다.
        String packageDescription,
        // 연관된 PO 식별자다.
        String poId,
        // 패키지에 포함할 활동 ID 목록이다.
        List<Long> activityIds,
        // 패키지를 볼 수 있는 사용자 ID 목록이다.
        List<Long> viewerIds
) {
    // 요청 DTO를 ActivityPackage 엔티티로 변환한다.
    public ActivityPackage toEntity(Long userId) {
        // null 입력을 빈 리스트로 바꿔 viewer 엔티티 목록을 만든다.
        List<ActivityPackageViewer> viewerList = viewerIds != null
                // 사용자 ID 목록을 viewer 엔티티 목록으로 변환한다.
                ? viewerIds.stream().map(ActivityPackageViewer::of).toList()
                // viewerIds가 없으면 빈 목록을 사용한다.
                : List.of();
        // null 입력을 빈 리스트로 바꿔 item 엔티티 목록을 만든다.
        List<ActivityPackageItem> itemList = activityIds != null
                // 활동 ID 목록을 item 엔티티 목록으로 변환한다.
                ? activityIds.stream().map(ActivityPackageItem::of).toList()
                // activityIds가 없으면 빈 목록을 사용한다.
                : List.of();
        // ActivityPackage 빌더를 열어 요청 값을 엔티티 필드로 복사한다.
        return ActivityPackage.builder()
                // 제목을 패키지 엔티티에 저장한다.
                .packageTitle(packageTitle)
                // 설명을 패키지 엔티티에 저장한다.
                .packageDescription(packageDescription)
                // PO ID를 패키지 엔티티에 저장한다.
                .poId(poId)
                // 요청 헤더 사용자 ID를 생성자로 저장한다.
                .creatorId(userId)
                // viewer 컬렉션을 초기값으로 설정한다.
                .viewers(viewerList)
                // activity item 컬렉션을 초기값으로 설정한다.
                .items(itemList)
                // 모든 필드 복사가 끝난 ActivityPackage 엔티티 생성을 마무리한다.
                .build();
    }
}
