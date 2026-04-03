package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.query.mapper.ActivityPackageQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityPackageQueryService {

    private final ActivityPackageQueryMapper activityPackageQueryMapper;
    private final AuthFeignClient authFeignClient;

    public ActivityPackage getPackage(Long packageId) {
        ActivityPackage activityPackage = activityPackageQueryMapper.findById(packageId);
        if (activityPackage == null) {
            throw new IllegalArgumentException("활동 패키지를 찾을 수 없습니다.");
        }
        return activityPackage;
    }

    public List<ActivityPackageResponse> getPackagesByViewerUserId(Long userId, Long creatorId, String poId) {
        List<ActivityPackage> packages = activityPackageQueryMapper.findAllByViewerUserId(userId, creatorId, poId);
        return packages.stream().map(this::enrichPackage).toList();
    }

    public List<ActivityPackage> getAllPackages() {
        return activityPackageQueryMapper.findAll();
    }

    public List<ActivityPackage> getPackagesByCreatorId(Long creatorId) {
        return activityPackageQueryMapper.findAllByCreatorId(creatorId);
    }

    public ActivityPackageResponse enrichPackage(ActivityPackage pkg) {
        String creatorName = fetchUserName(pkg.getCreatorId());
        return ActivityPackageResponse.from(pkg, creatorName);
    }

    private String fetchUserName(Long userId) {
        if (userId == null) return null;
        try {
            UserResponse user = authFeignClient.getUser(userId);
            return user != null ? user.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
