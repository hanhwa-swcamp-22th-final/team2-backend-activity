package com.team2.activity.command.service;

import com.team2.activity.command.repository.ActivityRepository;
import com.team2.activity.dto.ActivityUpdateRequest;
import com.team2.activity.entity.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityCommandService {

    private final ActivityRepository activityRepository;

    public Activity createActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    public Activity updateActivity(Long activityId, ActivityUpdateRequest request, Long authorId) {
        Activity activity = findById(activityId);
        activity.update(request.activityType(), request.activityTitle(), request.activityContent(),
                request.activityDate(), authorId, request.poId(), request.activityPriority(),
                request.activityScheduleFrom(), request.activityScheduleTo());
        return activity;
    }

    public void deleteActivity(Long activityId) {
        Activity activity = findById(activityId);
        activityRepository.delete(activity);
    }

    private Activity findById(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다."));
    }
}
