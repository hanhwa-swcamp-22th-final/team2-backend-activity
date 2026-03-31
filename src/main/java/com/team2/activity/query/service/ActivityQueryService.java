package com.team2.activity.query.service;

import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.query.mapper.ActivityQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityQueryService {

    private final ActivityQueryMapper activityQueryMapper;

    public Activity getActivity(Long activityId) {
        Activity activity = activityQueryMapper.findById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("활동을 찾을 수 없습니다.");
        }
        return activity;
    }

    public List<Activity> getAllActivities() {
        return activityQueryMapper.findAll();
    }

    public List<Activity> getActivitiesByClientId(Long clientId) {
        return activityQueryMapper.findByClientId(clientId);
    }

    public List<Activity> getActivitiesByActivityType(ActivityType activityType) {
        return activityQueryMapper.findByActivityType(activityType);
    }

    public List<Activity> getActivitiesByDateRange(LocalDate from, LocalDate to) {
        return activityQueryMapper.findByDateRange(from, to);
    }

    public List<Activity> getActivitiesByAuthorId(Long authorId) {
        return activityQueryMapper.findByAuthorId(authorId);
    }

    public List<Activity> getActivitiesByClientIdAndActivityType(Long clientId, ActivityType activityType) {
        return activityQueryMapper.findByClientIdAndActivityType(clientId, activityType);
    }
}
