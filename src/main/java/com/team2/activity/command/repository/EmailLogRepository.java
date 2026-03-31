// EmailLogRepository: EmailLog 엔티티의 Repository 인터페이스
package com.team2.activity.command.repository;

// EmailLog 엔티티 import
import com.team2.activity.entity.EmailLog;
// Spring Data JPA Repository import
import org.springframework.data.jpa.repository.JpaRepository;
// Spring Data JPA Specification 지원 import
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
// EmailLog 엔티티에 대한 Repository 인터페이스
// JpaRepository: 기본 CRUD 메서드 제공 (save, findById, deleteById 등)
// JpaSpecificationExecutor: Specification 기반 동적 쿼리 지원 (findAll(Specification, Pageable))
public interface EmailLogRepository extends JpaRepository<EmailLog, Long>, JpaSpecificationExecutor<EmailLog> {
    // Spring Data JPA가 메서드명 규칙으로 자동 구현
    // (추가 메서드 필요시 작성)
}
