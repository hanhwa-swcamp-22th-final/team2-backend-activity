package com.team2.activity.command.application.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.ActivityPackageViewer;
import com.team2.activity.command.domain.repository.ActivityPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.team2.activity.command.application.dto.ActivityPackageUpdateRequest;

// 활동 패키지 쓰기 유스케이스를 담당하는 command service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 쓰기 작업이 하나의 트랜잭션으로 처리되도록 보장한다.
@Transactional
public class ActivityPackageCommandService {

    // 활동 패키지 저장소 접근을 담당한다.
    private final ActivityPackageRepository activityPackageRepository;

    // 새 활동 패키지를 저장한다.
    public ActivityPackage createPackage(ActivityPackage activityPackage) {
        // 전달받은 패키지 엔티티를 저장소에 저장한다.
        return activityPackageRepository.save(activityPackage);
    }

    // 패키지 기본 정보와 viewer/item 목록을 한 번의 조회로 모두 수정한다.
    public ActivityPackage updateAll(Long packageId, ActivityPackageUpdateRequest request) {
        // 전체 수정 대상 패키지를 먼저 조회한다.
        ActivityPackage activityPackage = findById(packageId);
        // 제목, 설명, PO ID, 기간을 요청 값으로 갱신한다.
        activityPackage.update(request.packageTitle(), request.packageDescription(), request.poId(),
                request.dateFrom(), request.dateTo());
        // viewer ID가 전달되면 기존 viewer 목록을 새 목록으로 교체한다.
        if (request.viewerIds() != null) {
            activityPackage.getViewers().clear();
            activityPackage.getViewers().addAll(request.viewerIds().stream()
                    .map(ActivityPackageViewer::of)
                    .toList());
        }
        // activity ID가 전달되면 기존 item 목록을 새 목록으로 교체한다.
        if (request.activityIds() != null) {
            activityPackage.getItems().clear();
            activityPackage.getItems().addAll(request.activityIds().stream()
                    .map(ActivityPackageItem::of)
                    .toList());
        }
        // 변경 감지 대상 엔티티를 그대로 반환한다.
        return activityPackage;
    }

    // 패키지를 조회한 뒤 삭제한다.
    public void deletePackage(Long packageId) {
        // 삭제 대상 패키지를 먼저 조회한다.
        ActivityPackage activityPackage = findById(packageId);
        // 조회한 패키지를 저장소에서 삭제한다.
        activityPackageRepository.delete(activityPackage);
    }

    // ID로 패키지를 조회하고 없으면 예외를 던진다.
    private ActivityPackage findById(Long packageId) {
        return activityPackageRepository.findById(packageId)
                // 조회 결과가 없으면 패키지 없음 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."));
    }
}
