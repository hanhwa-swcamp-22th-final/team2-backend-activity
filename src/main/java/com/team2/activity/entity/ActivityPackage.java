package com.team2.activity.entity;

import lombok.Builder; // 빌더 패턴 자동 생성 어노테이션
import lombok.Getter;  // 모든 필드의 getter 메서드 자동 생성 어노테이션

import java.time.LocalDateTime; // 날짜+시간 타입 - 생성 일시에 사용
import java.util.ArrayList;     // 빈 리스트 초기화에 사용
import java.util.List;          // 열람 권한 사용자 ID, 활동기록 ID 목록 타입

// 활동기록 패키지 도메인 객체 (DB 테이블: activity_packages + activity_package_viewers + activity_package_items)
// 여러 활동기록을 하나의 묶음으로 관리하며, 특정 사용자에게 열람 권한을 부여할 수 있음
@Getter // 모든 필드의 getter 자동 생성 (getTitle(), getCreatorId() 등)
public class ActivityPackage {

    private final Long creatorId;          // 패키지 생성자 사용자 ID (생성 후 변경 불가 - final, FK→auth.users)
    private String title;                  // 패키지 제목 (필수)
    private String description;            // 패키지 설명 (선택)
    private String poId;                   // 연결된 수주건 ID (선택, FK→document.purchase_orders)
    private final List<Long> viewerIds;    // 열람 권한이 부여된 사용자 ID 목록 (FK→auth.users)
    private final List<Long> activityIds;  // 패키지에 포함된 활동기록 ID 목록 (FK→activities)
    private final LocalDateTime createdAt; // 패키지 생성 일시 (빌더 호출 시 자동으로 현재 시각 설정)

    @Builder // 이 생성자를 기반으로 빌더 클래스 자동 생성
    private ActivityPackage(String title, String description, String poId,
                            Long creatorId, List<Long> viewerIds, List<Long> activityIds) {
        this.title = title;                                                              // 제목 초기화
        this.description = description;                                                  // 설명 초기화
        this.poId = poId;                                                                // PO ID 초기화
        this.creatorId = creatorId;                                                      // 생성자 ID 초기화
        this.viewerIds = viewerIds != null ? viewerIds : new ArrayList<>();              // null 방어 - 빈 리스트로 초기화
        this.activityIds = activityIds != null ? activityIds : new ArrayList<>();        // null 방어 - 빈 리스트로 초기화
        this.createdAt = LocalDateTime.now();                                            // 생성 일시를 현재 시각으로 자동 설정
    }

    // 패키지 수정 메서드 - creatorId, viewerIds, activityIds, createdAt은 변경 불가
    public void update(String title, String description, String poId) {
        this.title = title;             // 제목 변경
        this.description = description; // 설명 변경 (null 전달 시 제거)
        this.poId = poId;               // PO ID 변경 (null 전달 시 연결 해제)
    }
}
