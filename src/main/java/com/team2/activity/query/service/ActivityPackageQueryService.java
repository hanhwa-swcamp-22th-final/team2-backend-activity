package com.team2.activity.query.service;

import com.team2.activity.entity.ActivityPackage;
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

    public ActivityPackage getPackage(Long packageId) {
        ActivityPackage activityPackage = activityPackageQueryMapper.findById(packageId);
        if (activityPackage == null) {
            throw new IllegalArgumentException("활동 패키지를 찾을 수 없습니다.");
        }
        return activityPackage;
    }

    public List<ActivityPackage> getAllPackages() {
        return activityPackageQueryMapper.findAll();
    }

    public List<ActivityPackage> getPackagesByCreatorId(Long creatorId) {
        return activityPackageQueryMapper.findAllByCreatorId(creatorId);
    }
}
