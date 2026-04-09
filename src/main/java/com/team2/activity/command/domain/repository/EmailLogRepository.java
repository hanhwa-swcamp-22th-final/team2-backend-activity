// EmailLogRepository: EmailLog 엔티티의 Repository 인터페이스
package com.team2.activity.command.domain.repository;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// EmailLog 엔티티에 대한 Repository 인터페이스
// JpaRepository: 기본 CRUD 메서드 제공 (save, findById, deleteById 등)
// JpaSpecificationExecutor: Specification 기반 동적 쿼리 지원 (findAll(Specification, Pageable))
public interface EmailLogRepository extends JpaRepository<EmailLog, Long>, JpaSpecificationExecutor<EmailLog> {

    /**
     * 재전송 시작 시 원자적으로 상태를 FAILED → SENDING 으로 전환한다.
     *
     * <p>동시에 여러 요청이 들어와도 하나만 성공하도록 DB UPDATE 의 WHERE 절에
     * {@code email_status = 'failed'} 조건을 포함한다. affected rows 가 0 이면
     * 이미 다른 요청이 진행 중이거나 상태가 바뀐 것이므로 호출자가 예외를 던져야 한다.
     *
     * @return 실제로 업데이트된 row 수. 1 이면 성공, 0 이면 경쟁 패배(이미 진행 중).
     */
    @Modifying
    @Query("""
            UPDATE EmailLog e
               SET e.emailStatus = :target
             WHERE e.emailLogId = :id
               AND e.emailStatus = :expected
            """)
    int transitionStatus(
            @Param("id") Long emailLogId,
            @Param("expected") MailStatus expected,
            @Param("target") MailStatus target);
}
