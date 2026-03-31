package com.team2.activity.command.service;

import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.entity.enums.Priority;
import com.team2.activity.command.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityCommandService {

    private final ActivityRepository activityRepository;

    public Activity createActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    public Activity updateActivity(Long activityId, ActivityType activityType, String activityTitle,
                                   String activityContent, LocalDate activityDate, Long activityAuthorId,
                                   String poId, Priority activityPriority,
                                   LocalDate activityScheduleFrom, LocalDate activityScheduleTo) {
        Activity activity = findById(activityId);
        activity.update(activityType, activityTitle, activityContent, activityDate,
                activityAuthorId, poId, activityPriority, activityScheduleFrom, activityScheduleTo);
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
