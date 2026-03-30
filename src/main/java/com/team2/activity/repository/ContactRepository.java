// ContactRepository: Contact 엔티티의 Repository 인터페이스
package com.team2.activity.repository;

// Contact 엔티티 import
import com.team2.activity.entity.Contact;
// Spring Data JPA Repository import
import org.springframework.data.jpa.repository.JpaRepository;
// 리스트 타입 import
import java.util.List;

// Contact 엔티티에 대한 Repository 인터페이스
// JpaRepository: 기본 CRUD 메서드 제공 (save, findById, deleteById 등)
public interface ContactRepository extends JpaRepository<Contact, Long> {
    // 거래처 ID로 모든 연락처 조회 메서드
    // Spring Data JPA가 메서드명 규칙으로 자동 구현
    // clientId와 일치하는 모든 Contact를 반환
    List<Contact> findAllByClientId(Long clientId);
}
