// ContactRepository: Contact 엔티티의 Repository 인터페이스
package com.team2.activity.command.domain.repository;

import com.team2.activity.command.domain.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Contact 엔티티 JpaRepository.
 * 컨택리스트는 거래처 무관 개인 주소록 — Contact 에서 client_id 컬럼이 제거됨에 따라
 * 기존 findAllByClientId 메서드도 함께 제거. 조회는 ContactQueryService(MyBatis) 가 담당.
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
