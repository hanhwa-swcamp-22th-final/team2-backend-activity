package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 활동 API 응답에 사용하는 DTO record다.
public record ActivityResponse(
        // 활동 고유 식별자다.
        Long activityId,
        // 연관 거래처 ID다.
        Long clientId,
        // 연관 PO ID다.
        String poId,
        // 활동 작성자 ID다.
        Long activityAuthorId,
        // 활동 날짜다.
        LocalDate activityDate,
        // 활동 유형이다.
        ActivityType activityType,
        // 활동 제목이다.
        String activityTitle,
        // 활동 상세 내용이다.
        String activityContent,
        // 활동 우선순위다.
        Priority activityPriority,
        // 일정 시작일이다.
        LocalDate activityScheduleFrom,
        // 일정 종료일이다.
        LocalDate activityScheduleTo,
        // 생성 시각이다.
        LocalDateTime createdAt,
        // 최종 수정 시각이다.
        LocalDateTime updatedAt,
        // 인증 서비스에서 조회한 작성자 이름이다 (상세 조회 시 채워진다).
        String authorName,
        // 마스터 서비스에서 조회한 거래처명이다 (상세 조회 시 채워진다).
        String clientName
) {
    // 목록 조회 등 외부 서비스 없이 엔티티만으로 DTO를 생성할 때 사용하는 팩터리 메서드다.
    public static ActivityResponse from(Activity activity) {
        // 이름 필드를 null로 두고 나머지 엔티티 값만 복사한다.
        return from(activity, null, null);
    }

    // 상세 조회 시 작성자 이름과 거래처명을 함께 받아 DTO를 생성하는 팩터리 메서드다.
    public static ActivityResponse from(Activity activity, String authorName, String clientName) {
        // 엔티티 각 필드를 레코드 생성자에 순서대로 넘겨 DTO를 구성한다.
        return new ActivityResponse(
                // 활동 식별자를 복사한다.
                activity.getActivityId(),
                // 거래처 ID를 복사한다.
                activity.getClientId(),
                // PO ID를 복사한다.
                activity.getPoId(),
                // 작성자 ID를 복사한다.
                activity.getActivityAuthorId(),
                // 활동 날짜를 복사한다.
                activity.getActivityDate(),
                // 활동 타입을 복사한다.
                activity.getActivityType(),
                // 활동 제목을 복사한다.
                activity.getActivityTitle(),
                // 활동 본문을 복사한다.
                activity.getActivityContent(),
                // 우선순위를 복사한다.
                activity.getActivityPriority(),
                // 일정 시작일을 복사한다.
                activity.getActivityScheduleFrom(),
                // 일정 종료일을 복사한다.
                activity.getActivityScheduleTo(),
                // 생성 시각을 복사한다.
                activity.getCreatedAt(),
                // 수정 시각을 복사한다.
                activity.getUpdatedAt(),
                // 외부 서비스에서 조회한 작성자 이름을 설정한다.
                authorName,
                // 외부 서비스에서 조회한 거래처명을 설정한다.
                clientName
        );
    }
}
