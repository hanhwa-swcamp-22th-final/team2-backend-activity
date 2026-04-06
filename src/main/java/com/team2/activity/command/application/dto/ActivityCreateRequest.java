package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// 활동 생성 요청 본문을 받는 DTO다.
@Schema(description = "활동 생성 요청")
public record ActivityCreateRequest(
        // 활동이 속한 거래처 ID다.
        @Schema(description = "거래처 ID", example = "1")
        @NotNull Long clientId,
        // 활동이 발생한 날짜다.
        @Schema(description = "활동 날짜", example = "2026-04-06")
        @NotNull LocalDate activityDate,
        // 활동 분류 값이다.
        @Schema(description = "활동 유형")
        @NotNull ActivityType activityType,
        // 활동 제목이다.
        @Schema(description = "활동 제목", example = "고객 미팅")
        @NotBlank String activityTitle
) {
    // 요청 DTO를 저장 가능한 Activity 엔티티로 변환한다.
    public Activity toEntity(Long userId) {
        // Activity 빌더를 열어 요청 값을 엔티티 필드로 복사한다.
        return Activity.builder()
                // 요청의 거래처 ID를 엔티티에 복사한다.
                .clientId(clientId)
                // 요청의 활동 날짜를 엔티티에 복사한다.
                .activityDate(activityDate)
                // 요청의 활동 타입을 엔티티에 복사한다.
                .activityType(activityType)
                // 요청의 제목을 엔티티에 복사한다.
                .activityTitle(activityTitle)
                // 헤더에서 받은 사용자 ID를 작성자로 저장한다.
                .activityAuthorId(userId)
                // 모든 필드 복사가 끝난 Activity 엔티티 생성을 마무리한다.
                .build();
    }
}
