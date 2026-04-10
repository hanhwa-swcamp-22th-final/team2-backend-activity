package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.query.dto.ActivityPackageResponse;
import com.team2.activity.query.mapper.ActivityPackageQueryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityPackageQueryService {

    private final ActivityPackageQueryMapper activityPackageQueryMapper;
    private final AuthFeignClient authFeignClient;

    public ActivityPackage getPackage(Long packageId) {
        ActivityPackage activityPackage = activityPackageQueryMapper.findActivityPackageById(packageId);
        if (activityPackage == null) {
            throw new IllegalArgumentException("활동 패키지를 찾을 수 없습니다.");
        }
        return activityPackage;
    }

    public ActivityPackageResponse getEnrichedPackage(Long packageId) {
        return enrichPackage(getPackage(packageId));
    }

    public List<ActivityPackageResponse> getPackagesByViewerUserId(Long userId, Long creatorId, String poId) {
        List<ActivityPackage> packages = activityPackageQueryMapper.findAllActivityPackagesByViewerUserId(userId, creatorId, poId);
        return packages.stream().map(this::enrichPackage).toList();
    }

    public List<ActivityPackageResponse> getPackagesWithFilters(Long creatorId, String poId) {
        List<ActivityPackage> packages = activityPackageQueryMapper.findAllActivityPackagesWithFilters(creatorId, poId);
        return packages.stream().map(this::enrichPackage).toList();
    }

    public List<ActivityPackageResponse> getPackagesWithFilters(Long creatorId, String poId, int page, int size) {
        int offset = page * size;
        List<ActivityPackage> packages = activityPackageQueryMapper.findActivityPackagesWithFilters(creatorId, poId, size, offset);
        return packages.stream().map(this::enrichPackage).toList();
    }

    public long countPackagesWithFilters(Long creatorId, String poId) {
        return activityPackageQueryMapper.countActivityPackagesWithFilters(creatorId, poId);
    }

    public List<ActivityPackage> getAllPackages() {
        return activityPackageQueryMapper.findAllActivityPackages();
    }

    public List<ActivityPackage> getPackagesByCreatorId(Long creatorId) {
        return activityPackageQueryMapper.findAllActivityPackagesByCreatorId(creatorId);
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
            log.warn("사용자 이름 조회 실패 [userId={}]: {}", userId, e.getMessage());
            return null;
        }
    }
}
