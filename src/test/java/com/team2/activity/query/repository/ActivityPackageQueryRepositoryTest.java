package com.team2.activity.query.repository;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.ActivityPackageViewer;
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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// ActivityPackage 조회 mapper가 본문과 하위 컬렉션을 함께 매핑하는지 검증한다.
@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@Transactional
@DisplayName("ActivityPackageQueryRepository 테스트")
class ActivityPackageQueryRepositoryTest {

    // 패키지 읽기 전용 쿼리를 수행할 실제 mapper다.
    @Autowired
    private ActivityPackageQueryMapper activityPackageQueryMapper;

    // 테스트용 패키지 데이터를 직접 적재할 JDBC 도구다.
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 패키지와 연관 컬렉션 데이터를 모두 저장하고 mapper 결과를 반환한다.
    private ActivityPackage savePackage(String title, Long creatorId, List<Long> viewerIds, List<Long> activityIds) {
        // activity_packages 본문 레코드를 먼저 저장한다.
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

        // 방금 저장된 패키지의 PK를 조회한다.
        Long packageId = jdbcTemplate.queryForObject("SELECT MAX(package_id) FROM activity_packages", Long.class);

        // 패키지 열람자 관계 데이터를 추가한다.
        for (Long viewerId : viewerIds) {
            jdbcTemplate.update(
                    "INSERT INTO activity_package_viewers (package_id, user_id) VALUES (?, ?)",
                    packageId,
                    viewerId
            );
        }

        // 패키지에 포함된 활동 관계 데이터를 추가한다.
        for (Long activityId : activityIds) {
            jdbcTemplate.update(
                    "INSERT INTO activity_package_items (package_id, activity_id) VALUES (?, ?)",
                    packageId,
                    activityId
            );
        }

        // 최종적으로 mapper 상세 조회 결과를 반환한다.
        return activityPackageQueryMapper.findActivityPackageById(packageId);
    }

    @Test
    @DisplayName("ID로 패키지 조회 시 열람자와 항목 목록이 매핑된다")
    void findById_mapsPackageFieldsAndCollections() {
        // 열람자와 활동 항목이 포함된 패키지를 저장한다.
        ActivityPackage saved = savePackage("주간 패키지", 7L, List.of(2L, 3L), List.of(100L, 101L));

        // mapper가 하위 컬렉션까지 함께 읽어 오는지 확인한다.
        ActivityPackage found = activityPackageQueryMapper.findActivityPackageById(saved.getPackageId());

        // 패키지 본문과 컬렉션 필드가 모두 정확히 매핑되는지 검증한다.
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
        // 서로 다른 creator_id를 가진 패키지들을 저장한다.
        savePackage("생성자7 패키지1", 7L, List.of(2L), List.of(100L));
        savePackage("생성자7 패키지2", 7L, List.of(3L), List.of(101L));
        savePackage("생성자8 패키지", 8L, List.of(4L), List.of(102L));

        // creator_id 조건으로 패키지 목록을 조회한다.
        List<ActivityPackage> result = activityPackageQueryMapper.findAllActivityPackagesByCreatorId(7L);

        // 요청한 생성자의 패키지만 반환되는지 확인한다.
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ActivityPackage::getCreatorId)
                .containsOnly(7L);
    }
}
