// EmailLogSpecification: EmailLog 엔티티의 동적 쿼리 필터링 정의
package com.team2.activity.command.repository;

// EmailLog 엔티티 import
import com.team2.activity.entity.EmailLog;
// MailStatus 열거형 import
import com.team2.activity.entity.enums.MailStatus;
// Spring Data JPA Specification import
import org.springframework.data.jpa.domain.Specification;

// EmailLog 엔티티의 Specification 필터 메서드 정의 클래스
public class EmailLogSpecification {

    // 거래처 ID로 필터링하는 Specification
    public static Specification<EmailLog> withClientId(Long clientId) {
        // Specification 람다식으로 WHERE 조건 정의
        return (root, query, criteriaBuilder) ->
                // clientId 필드가 주어진 값과 같은지 확인
                criteriaBuilder.equal(root.get("clientId"), clientId);
    }

    // 이메일 상태로 필터링하는 Specification
    public static Specification<EmailLog> withEmailStatus(MailStatus emailStatus) {
        // Specification 람다식으로 WHERE 조건 정의
        return (root, query, criteriaBuilder) ->
                // emailStatus 필드가 주어진 값과 같은지 확인
                criteriaBuilder.equal(root.get("emailStatus"), emailStatus);
    }
}
