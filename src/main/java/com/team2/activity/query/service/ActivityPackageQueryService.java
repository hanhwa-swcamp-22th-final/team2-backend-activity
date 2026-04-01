package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.query.mapper.ActivityPackageQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 활동 패키지 읽기 유스케이스를 담당하는 query service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 읽기 전용 트랜잭션으로 조회 성격을 명확히 한다.
@Transactional(readOnly = true)
public class ActivityPackageQueryService {

    // 활동 패키지 조회용 MyBatis mapper다.
    private final ActivityPackageQueryMapper activityPackageQueryMapper;

    // 패키지 ID로 단건을 조회하고 없으면 예외를 던진다.
    public ActivityPackage getPackage(Long packageId) {
        // mapper를 호출해 packageId에 해당하는 패키지를 조회한다.
        ActivityPackage activityPackage = activityPackageQueryMapper.findById(packageId);
        // 조회 결과가 없으면 단건 조회 실패 예외를 던진다.
        if (activityPackage == null) {
            throw new IllegalArgumentException("활동 패키지를 찾을 수 없습니다.");
        }
        // 조회된 패키지 엔티티를 그대로 반환한다.
        return activityPackage;
    }

    // 전체 활동 패키지 목록을 조회한다.
    public List<ActivityPackage> getAllPackages() {
        // 전체 패키지 목록 조회를 mapper에 위임한다.
        return activityPackageQueryMapper.findAll();
    }

    // 생성자 ID로 활동 패키지 목록을 조회한다.
    public List<ActivityPackage> getPackagesByCreatorId(Long creatorId) {
        // 생성자 조건 목록 조회를 mapper에 위임한다.
        return activityPackageQueryMapper.findAllByCreatorId(creatorId);
    }
}
