package com.team2.activity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 패키지와 활동의 포함 관계를 저장하는 보조 엔티티다.
@Entity
// 매핑 대상 테이블 이름을 지정한다.
@Table(name = "activity_package_items")
// 읽기 전용 필드 접근을 위한 getter를 생성한다.
@Getter
// JPA 기본 생성자를 보호 수준으로 제공한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityPackageItem {

    // 패키지 항목 기본키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_item_id")
    private Long packageItemId;

    // 패키지에 포함된 활동 ID다.
    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    // 정적 팩터리 사용을 강제하기 위한 private 생성자다.
    private ActivityPackageItem(Long activityId) {
        // 전달받은 활동 ID를 엔티티 필드에 저장한다.
        this.activityId = activityId;
    }

    // 활동 ID만으로 패키지 항목 엔티티를 생성한다.
    public static ActivityPackageItem of(Long activityId) {
        // private 생성자를 호출해 새 패키지 항목 엔티티를 만든다.
        return new ActivityPackageItem(activityId);
    }
}
