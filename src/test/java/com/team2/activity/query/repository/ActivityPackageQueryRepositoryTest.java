package com.team2.activity.query.repository;

import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.entity.ActivityPackageItem;
import com.team2.activity.entity.ActivityPackageViewer;
import com.team2.activity.query.mapper.ActivityPackageQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@DisplayName("ActivityPackageQueryRepository 테스트")
class ActivityPackageQueryRepositoryTest {

    @Autowired
    private ActivityPackageQueryMapper activityPackageQueryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ActivityPackage savePackage(String title, Long creatorId, List<Long> viewerIds, List<Long> activityIds) {
        jdbcTemplate.update(
                """
                INSERT INTO activity_packages (
                    package_title, package_description, po_id, creator_id, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                title,
                title + " 설명",
                "PO-" + creatorId,
                creatorId,
                Timestamp.valueOf(LocalDateTime.of(2025, 4, 1, 9, 0)),
                Timestamp.valueOf(LocalDateTime.of(2025, 4, 1, 9, 0))
        );

        Long packageId = jdbcTemplate.queryForObject("SELECT MAX(package_id) FROM activity_packages", Long.class);

        for (Long viewerId : viewerIds) {
            jdbcTemplate.update(
                    "INSERT INTO activity_package_viewers (package_id, user_id) VALUES (?, ?)",
                    packageId,
                    viewerId
            );
        }

        for (Long activityId : activityIds) {
            jdbcTemplate.update(
                    "INSERT INTO activity_package_items (package_id, activity_id) VALUES (?, ?)",
                    packageId,
                    activityId
            );
        }

        return activityPackageQueryMapper.findById(packageId);
    }

    @Test
    @DisplayName("ID로 패키지 조회 시 열람자와 항목 목록이 매핑된다")
    void findById_mapsPackageFieldsAndCollections() {
        ActivityPackage saved = savePackage("주간 패키지", 7L, List.of(2L, 3L), List.of(100L, 101L));

        ActivityPackage found = activityPackageQueryMapper.findById(saved.getPackageId());

        assertThat(found).isNotNull();
        assertThat(found.getPackageTitle()).isEqualTo("주간 패키지");
        assertThat(found.getCreatorId()).isEqualTo(7L);
        assertThat(found.getViewers())
                .extracting(ActivityPackageViewer::getUserId)
                .containsExactlyInAnyOrder(2L, 3L);
        assertThat(found.getItems())
                .extracting(ActivityPackageItem::getActivityId)
                .containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    @DisplayName("생성자 ID로 패키지 목록을 조회한다")
    void findAllByCreatorId_returnsOnlyMatchedPackages() {
        savePackage("생성자7 패키지1", 7L, List.of(2L), List.of(100L));
        savePackage("생성자7 패키지2", 7L, List.of(3L), List.of(101L));
        savePackage("생성자8 패키지", 8L, List.of(4L), List.of(102L));

        List<ActivityPackage> result = activityPackageQueryMapper.findAllByCreatorId(7L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ActivityPackage::getCreatorId)
                .containsOnly(7L);
    }
}
