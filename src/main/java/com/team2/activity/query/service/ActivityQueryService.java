package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.query.mapper.ActivityQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// 활동 읽기 유스케이스를 담당하는 query service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 읽기 전용 트랜잭션으로 조회 성격을 명확히 한다.
@Transactional(readOnly = true)
public class ActivityQueryService {

    // 활동 조회용 MyBatis mapper다.
    private final ActivityQueryMapper activityQueryMapper;

    // 활동 ID로 단건을 조회하고 없으면 예외를 던진다.
    public Activity getActivity(Long activityId) {
        // mapper를 호출해 activityId에 해당하는 활동을 조회한다.
        Activity activity = activityQueryMapper.findById(activityId);
        // 조회 결과가 없으면 단건 조회 실패 예외를 던진다.
        if (activity == null) {
            throw new IllegalArgumentException("활동을 찾을 수 없습니다.");
        }
        // 조회된 활동 엔티티를 그대로 반환한다.
        return activity;
    }

    // 전체 활동 목록을 조회한다.
    public List<Activity> getAllActivities() {
        // 전체 활동 목록 조회를 mapper에 위임한다.
        return activityQueryMapper.findAll();
    }

    // 거래처 ID로 활동 목록을 조회한다.
    public List<Activity> getActivitiesByClientId(Long clientId) {
        // 거래처 조건 목록 조회를 mapper에 위임한다.
        return activityQueryMapper.findByClientId(clientId);
    }

    // 활동 타입으로 활동 목록을 조회한다.
    public List<Activity> getActivitiesByActivityType(ActivityType activityType) {
        // 활동 타입 조건 목록 조회를 mapper에 위임한다.
        return activityQueryMapper.findByActivityType(activityType);
    }

    // 날짜 범위로 활동 목록을 조회한다.
    public List<Activity> getActivitiesByDateRange(LocalDate from, LocalDate to) {
        // 양쪽 경계값이 모두 있어야 범위 조회가 가능하다.
        if (from == null || to == null) {
            throw new IllegalArgumentException("날짜 범위(from, to)는 필수입니다.");
        }
        // 날짜 범위 조건 목록 조회를 mapper에 위임한다.
        return activityQueryMapper.findByDateRange(from, to);
    }

    // 작성자 ID로 활동 목록을 조회한다.
    public List<Activity> getActivitiesByAuthorId(Long authorId) {
        // 작성자 조건 목록 조회를 mapper에 위임한다.
        return activityQueryMapper.findByAuthorId(authorId);
    }

    // 거래처 ID와 활동 타입을 함께 사용해 활동 목록을 조회한다.
    public List<Activity> getActivitiesByClientIdAndActivityType(Long clientId, ActivityType activityType) {
        // 거래처와 활동 타입 복합 조건 조회를 mapper에 위임한다.
        return activityQueryMapper.findByClientIdAndActivityType(clientId, activityType);
    }
}
