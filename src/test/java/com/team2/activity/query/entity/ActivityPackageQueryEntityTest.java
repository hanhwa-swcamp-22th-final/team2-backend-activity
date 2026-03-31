package com.team2.activity.query.entity;

import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.entity.ActivityPackageItem;
import com.team2.activity.entity.ActivityPackageViewer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Query ActivityPackage 엔티티 테스트")
class ActivityPackageQueryEntityTest {

    @Test
    @DisplayName("조회용 ActivityPackage는 열람자와 항목 목록을 가진다")
    void createReadPackage_withViewersAndItems() {
        ActivityPackage activityPackage = ActivityPackage.builder()
                .packageTitle("주간 패키지")
                .packageDescription("조회용 패키지")
                .poId("PO-001")
                .creatorId(7L)
                .viewers(List.of(ActivityPackageViewer.of(2L), ActivityPackageViewer.of(3L)))
                .items(List.of(ActivityPackageItem.of(100L), ActivityPackageItem.of(101L)))
                .build();

        assertThat(activityPackage.getPackageTitle()).isEqualTo("주간 패키지");
        assertThat(activityPackage.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactly(2L, 3L);
        assertThat(activityPackage.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactly(100L, 101L);
    }

    @Test
    @DisplayName("조회용 ActivityPackage는 null 컬렉션을 빈 리스트로 초기화한다")
    void createReadPackage_withEmptyCollections() {
        ActivityPackage activityPackage = ActivityPackage.builder()
                .packageTitle("빈 패키지")
                .creatorId(7L)
                .viewers(null)
                .items(null)
                .build();

        assertThat(activityPackage.getViewers()).isNotNull().isEmpty();
        assertThat(activityPackage.getItems()).isNotNull().isEmpty();
    }
}
