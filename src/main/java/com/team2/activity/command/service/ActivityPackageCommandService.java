package com.team2.activity.command.service;

import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.entity.ActivityPackageItem;
import com.team2.activity.entity.ActivityPackageViewer;
import com.team2.activity.command.repository.ActivityPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityPackageCommandService {

    private final ActivityPackageRepository activityPackageRepository;

    public ActivityPackage createPackage(ActivityPackage activityPackage) {
        return activityPackageRepository.save(activityPackage);
    }

    public ActivityPackage updatePackage(Long packageId, String packageTitle,
                                         String packageDescription, String poId) {
        ActivityPackage activityPackage = findById(packageId);
        activityPackage.update(packageTitle, packageDescription, poId);
        return activityPackage;
    }

    public ActivityPackage updateViewers(Long packageId, List<Long> viewerUserIds) {
        ActivityPackage activityPackage = findById(packageId);
        activityPackage.getViewers().clear();
        List<ActivityPackageViewer> viewers = viewerUserIds.stream()
                .map(ActivityPackageViewer::of)
                .toList();
        activityPackage.getViewers().addAll(viewers);
        return activityPackage;
    }

    public ActivityPackage updateItems(Long packageId, List<Long> activityIds) {
        ActivityPackage activityPackage = findById(packageId);
        activityPackage.getItems().clear();
        List<ActivityPackageItem> items = activityIds.stream()
                .map(ActivityPackageItem::of)
                .toList();
        activityPackage.getItems().addAll(items);
        return activityPackage;
    }

    public void deletePackage(Long packageId) {
        ActivityPackage activityPackage = findById(packageId);
        activityPackageRepository.delete(activityPackage);
    }

    private ActivityPackage findById(Long packageId) {
        return activityPackageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."));
    }
}
