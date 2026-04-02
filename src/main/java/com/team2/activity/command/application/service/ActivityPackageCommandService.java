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
        // 제목, 설명, PO ID를 요청 값으로 갱신한다.
        activityPackage.update(request.packageTitle(), request.packageDescription(), request.poId());
        // viewer ID가 전달되면 기존 viewer 목록을 새 목록으로 교체한다.
        if (request.viewerIds() != null) {
            // 기존 viewer 연결을 모두 제거한다.
            activityPackage.getViewers().clear();
            // 새 viewer ID 목록을 viewer 엔티티 목록으로 바꿔 채운다.
            activityPackage.getViewers().addAll(request.viewerIds().stream()
                    // 각 사용자 ID를 viewer 엔티티로 변환한다.
                    .map(ActivityPackageViewer::of)
                    // 변환된 viewer 엔티티들을 리스트로 모은다.
                    .toList());
        }
        // null 활동 ID 목록은 빈 목록으로 처리한다.
        List<Long> activityIds = request.activityIds() != null ? request.activityIds() : List.of();
        // item 목록은 항상 요청 기준으로 다시 구성한다.
        activityPackage.getItems().clear();
        activityPackage.getItems().addAll(activityIds.stream()
                // 각 활동 ID를 item 엔티티로 변환한다.
                .map(ActivityPackageItem::of)
                // 변환된 item 엔티티들을 리스트로 모은다.
                .toList());
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
