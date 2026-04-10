package com.team2.activity.command.application.service;

import com.team2.activity.command.application.dto.ActivityCreateRequest;
import com.team2.activity.command.application.dto.ActivityUpdateRequest;
import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.repository.ActivityRepository;
import com.team2.activity.query.dto.ActivityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 활동 쓰기 유스케이스를 담당하는 command service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 쓰기 작업이 하나의 트랜잭션으로 처리되도록 보장한다.
@Transactional
public class ActivityCommandService {

    // 활동 저장소 접근을 담당한다.
    private final ActivityRepository activityRepository;

    // 새 활동을 생성하고 응답 DTO를 반환한다.
    public ActivityResponse createActivity(ActivityCreateRequest request, Long userId) {
        Activity activity = activityRepository.save(request.toEntity(userId));
        return ActivityResponse.from(activity);
    }

    // 수정 대상 활동을 찾아 요청 값으로 갱신한다.
    public ActivityResponse updateActivity(Long activityId, ActivityUpdateRequest request, Long authorId) {
        // 먼저 수정 대상 활동이 존재하는지 확인한다.
        Activity activity = findById(activityId);
        // 요청 값과 수정자 정보를 사용해 엔티티 상태를 바꾼다.
        activity.update(
                // 요청의 활동 타입을 반영한다.
                request.activityType(),
                // 요청의 활동 제목을 반영한다.
                request.activityTitle(),
                // 요청의 활동 본문을 반영한다.
                request.activityContent(),
                // 요청의 활동 날짜를 반영한다.
                request.activityDate(),
                // 헤더에서 받은 수정자 ID를 반영한다.
                authorId,
                // 요청의 PO ID를 반영한다.
                request.poId(),
                // 요청의 우선순위를 반영한다.
                request.activityPriority(),
                // 요청의 일정 시작일을 반영한다.
                request.activityScheduleFrom(),
                // 요청의 일정 종료일을 반영한다.
                request.activityScheduleTo()
        );
        return ActivityResponse.from(activity);
    }

    // 활동을 조회한 뒤 삭제한다.
    public void deleteActivity(Long activityId) {
        // 삭제 대상 활동 엔티티를 먼저 조회한다.
        Activity activity = findById(activityId);
        // 조회한 활동 엔티티를 저장소에서 삭제한다.
        activityRepository.delete(activity);
    }

    // ID로 활동을 조회하고 없으면 예외를 던진다.
    private Activity findById(Long activityId) {
        return activityRepository.findById(activityId)
                // 조회 결과가 없으면 활동 없음 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다."));
    }
}
