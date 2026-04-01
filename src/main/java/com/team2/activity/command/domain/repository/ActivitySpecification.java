// ActivitySpecification: Activity 엔티티의 동적 쿼리 필터링 정의
package com.team2.activity.command.domain.repository;

// Activity 엔티티 import
import com.team2.activity.command.domain.entity.Activity;
// ActivityType 열거형 import
import com.team2.activity.command.domain.entity.enums.ActivityType;
// Spring Data JPA Specification import
import org.springframework.data.jpa.domain.Specification;

// 날짜 타입 import
import java.time.LocalDate;

// Activity 엔티티의 Specification 필터 메서드 정의 클래스
public class ActivitySpecification {

    // 거래처 ID로 필터링하는 Specification
    public static Specification<Activity> withClientId(Long clientId) {
        // Specification 람다식으로 WHERE 조건 정의
        return (root, query, criteriaBuilder) ->
                // clientId 필드가 주어진 값과 같은지 확인
                criteriaBuilder.equal(root.get("clientId"), clientId);
    }

    // 활동 타입으로 필터링하는 Specification
    public static Specification<Activity> withActivityType(ActivityType activityType) {
        // Specification 람다식으로 WHERE 조건 정의
        return (root, query, criteriaBuilder) ->
                // activityType 필드가 주어진 값과 같은지 확인
                criteriaBuilder.equal(root.get("activityType"), activityType);
    }

    // 날짜 범위로 필터링하는 Specification
    public static Specification<Activity> withDateRange(LocalDate from, LocalDate to) {
        // Specification 람다식으로 WHERE 조건 정의
        return (root, query, criteriaBuilder) ->
                // activityDate가 from과 to 사이에 있는지 확인
                criteriaBuilder.between(root.get("activityDate"), from, to);
    }

    // 작성자 ID로 필터링하는 Specification
    public static Specification<Activity> withAuthorId(Long authorId) {
        // Specification 람다식으로 WHERE 조건 정의
        return (root, query, criteriaBuilder) ->
                // activityAuthorId 필드가 주어진 값과 같은지 확인
                criteriaBuilder.equal(root.get("activityAuthorId"), authorId);
    }
}
