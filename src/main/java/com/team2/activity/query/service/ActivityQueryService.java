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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
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
                                                            LocalDate activityDateTo, String keyword,
                                                            int page, int size) {
        int offset = page * size;
        List<Activity> activities = activityQueryMapper.findWithFilters(
                clientId, poId, activityType, activityAuthorId, activityDateFrom, activityDateTo, keyword, size, offset);
        return enrichActivities(activities);
    }

    public long countWithFilters(Long clientId, String poId, ActivityType activityType,
                                  Long activityAuthorId, LocalDate activityDateFrom,
                                  LocalDate activityDateTo, String keyword) {
        return activityQueryMapper.countWithFilters(clientId, poId, activityType, activityAuthorId, activityDateFrom, activityDateTo, keyword);
    }

    private List<ActivityResponse> enrichActivities(List<Activity> activities) {
        Set<Long> authorIds = activities.stream().map(Activity::getActivityAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> clientIds = activities.stream().map(Activity::getClientId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> authorNames = new HashMap<>();
        authorIds.forEach(id -> authorNames.put(id, fetchUserName(id)));

        Map<Long, String> clientNames = new HashMap<>();
        clientIds.forEach(id -> clientNames.put(id, fetchClientName(id)));

        return activities.stream().map(a -> ActivityResponse.from(a,
                authorNames.get(a.getActivityAuthorId()),
                clientNames.get(a.getClientId())
        )).toList();
    }

    public String fetchUserName(Long userId) {
        if (userId == null) return null;
        try {
            UserResponse user = authFeignClient.getUser(userId);
            return user != null ? user.getName() : null;
        } catch (Exception e) {
            log.warn("사용자 이름 조회 실패 [userId={}]: {}", userId, e.getMessage());
            return null;
        }
    }

    public String fetchClientName(Long clientId) {
        if (clientId == null) return null;
        try {
            ClientResponse client = masterFeignClient.getClient(clientId);
            return client != null ? client.getName() : null;
        } catch (Exception e) {
            log.warn("거래처명 조회 실패 [clientId={}]: {}", clientId, e.getMessage());
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
