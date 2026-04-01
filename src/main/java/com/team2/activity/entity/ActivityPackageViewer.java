package com.team2.activity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 패키지 열람 권한 사용자 관계를 저장하는 보조 엔티티다.
@Entity
// 매핑 대상 테이블 이름을 지정한다.
@Table(name = "activity_package_viewers")
// 읽기 전용 필드 접근을 위한 getter를 생성한다.
@Getter
// JPA 기본 생성자를 보호 수준으로 제공한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityPackageViewer {

    // 패키지 열람자 기본키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_viewer_id")
    private Long packageViewerId;

    // 열람 권한을 가진 사용자 ID다.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 정적 팩터리 사용을 강제하기 위한 private 생성자다.
    private ActivityPackageViewer(Long userId) {
        // 전달받은 사용자 ID를 엔티티 필드에 저장한다.
        this.userId = userId;
    }

    // 사용자 ID만으로 열람자 엔티티를 생성한다.
    public static ActivityPackageViewer of(Long userId) {
        // private 생성자를 호출해 새 열람자 엔티티를 만든다.
        return new ActivityPackageViewer(userId);
    }
}
