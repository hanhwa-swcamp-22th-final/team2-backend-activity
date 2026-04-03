package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.ClientResponse;
import com.team2.activity.command.infrastructure.client.MasterFeignClient;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.query.dto.ActivityResponse;
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
    private final AuthFeignClient authFeignClient;
    private final MasterFeignClient masterFeignClient;

    public ActivityResponse getActivity(Long activityId) {
        Activity activity = activityQueryMapper.findById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("활동을 찾을 수 없습니다.");
        }
        String authorName = fetchUserName(activity.getActivityAuthorId());
        String clientName = fetchClientName(activity.getClientId());
        return ActivityResponse.from(activity, authorName, clientName);
    }

    public List<ActivityResponse> getActivitiesWithFilters(Long clientId, String poId, ActivityType activityType,
                                                            Long activityAuthorId, LocalDate activityDateFrom,
                                                            LocalDate activityDateTo, String keyword) {
        List<Activity> activities = activityQueryMapper.findWithFilters(
                clientId, poId, activityType, activityAuthorId, activityDateFrom, activityDateTo, keyword);
        return activities.stream().map(this::enrichActivity).toList();
    }

    private ActivityResponse enrichActivity(Activity activity) {
        String authorName = fetchUserName(activity.getActivityAuthorId());
        String clientName = fetchClientName(activity.getClientId());
        return ActivityResponse.from(activity, authorName, clientName);
    }

    public String fetchUserName(Long userId) {
        if (userId == null) return null;
        try {
            UserResponse user = authFeignClient.getUser(userId);
            return user != null ? user.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String fetchClientName(Long clientId) {
        if (clientId == null) return null;
        try {
            ClientResponse client = masterFeignClient.getClient(clientId);
            return client != null ? client.getName() : null;
        } catch (Exception e) {
            return null;
        }
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
        if (from == null || to == null) {
            throw new IllegalArgumentException("날짜 범위(from, to)는 필수입니다.");
        }
        return activityQueryMapper.findByDateRange(from, to);
    }

    public List<Activity> getActivitiesByAuthorId(Long authorId) {
        return activityQueryMapper.findByAuthorId(authorId);
    }

    public List<Activity> getActivitiesByClientIdAndActivityType(Long clientId, ActivityType activityType) {
        return activityQueryMapper.findByClientIdAndActivityType(clientId, activityType);
    }
}
