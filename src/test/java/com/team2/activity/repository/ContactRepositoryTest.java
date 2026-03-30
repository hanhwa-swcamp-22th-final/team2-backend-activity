// ContactRepositoryTest: Contact 엔티티의 Repository 계층 테스트
package com.team2.activity.repository;

// Contact 엔티티 import
import com.team2.activity.entity.Contact;
// 테스트 메서드 표시 import
import org.junit.jupiter.api.DisplayName;
// 테스트 메서드 import
import org.junit.jupiter.api.Test;
// 의존성 주입 import
import org.springframework.beans.factory.annotation.Autowired;
// JPA 테스트 설정 import
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// 테스트 프로파일 활성화 import
import org.springframework.test.context.ActiveProfiles;

// 리스트 타입 import
import java.util.List;

// AssertJ assertion import
import static org.assertj.core.api.Assertions.assertThat;

// JPA 테스트 설정 어노테이션
@DataJpaTest
// 테스트 프로파일 설정
@ActiveProfiles("test")
// 테스트 클래스 표시명
@DisplayName("ContactRepository 테스트")
// ContactRepository 테스트 클래스
class ContactRepositoryTest {

    // 테스트 대상 Repository 주입
    @Autowired
    private ContactRepository contactRepository;

    // ── 공통 픽스처 (테스트용 객체 생성 헬퍼 메서드) ─────────────────────────────────────────────
    // 기본 Contact 객체 생성 헬퍼 메서드
    private Contact buildContact(Long clientId, Long writerId, String name) {
        // Contact 빌더 패턴으로 객체 생성
        return Contact.builder()
                // 거래처 ID 설정
                .clientId(clientId)
                // 작성자 ID 설정
                .writerId(writerId)
                // 연락처 이름 설정
                .contactName(name)
                // 연락처 직급 설정 (고정값)
                .contactPosition("Team Leader")
                // 연락처 이메일 설정 (이름을 소문자로 변환하여 설정)
                .contactEmail(name.toLowerCase() + "@example.com")
                // 연락처 전화번호 설정 (고정값)
                .contactTel("010-1234-5678")
                // 빌드 완료
                .build();
    }

    // ── 테스트 1: 저장 및 ID 조회 ───────────────────────────────
    // 연락처 저장 후 ID로 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("연락처 저장 후 ID로 조회")
    // 테스트 메서드
    void saveAndFindById() {
        // 기본 연락처 객체 생성 (거래처ID=1, 작성자ID=10, 이름=김철수)
        Contact contact = buildContact(1L, 10L, "김철수");

        // 연락처 객체 저장
        Contact saved = contactRepository.save(contact);

        // PK 자동 생성 확인
        assertThat(saved.getContactId()).isNotNull();
        // 저장된 ID로 조회하여 이름이 일치하는지 확인
        assertThat(contactRepository.findById(saved.getContactId()))
                // Optional이 존재하는지 확인
                .isPresent()
                // Optional에서 값 추출
                .get()
                // 연락처 이름 추출
                .extracting(Contact::getContactName)
                // 이름이 "김철수"와 일치하는지 확인
                .isEqualTo("김철수");
    }

    // ── 테스트 2: clientId로 전체 목록 조회 (페이징 없음) ────────
    // 거래처 ID로 연락처 전체 목록을 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("clientId로 연락처 전체 목록 조회")
    // 테스트 메서드
    void findAllByClientId() {
        // 거래처 ID=1, 이름=김철수 연락처 저장
        contactRepository.save(buildContact(1L, 10L, "김철수"));
        // 거래처 ID=1, 이름=이영희 연락처 저장
        contactRepository.save(buildContact(1L, 10L, "이영희"));
        // 거래처 ID=2, 이름=박민수 연락처 저장 (다른 거래처)
        contactRepository.save(buildContact(2L, 10L, "박민수"));

        // 거래처 ID=1로 연락처 목록 조회
        List<Contact> result = contactRepository.findAllByClientId(1L);

        // 거래처 ID=1인 연락처가 2개인지 확인
        assertThat(result).hasSize(2);
        // 조회된 연락처들의 이름이 김철수와 이영희인지 확인
        assertThat(result)
                // 연락처 이름 추출
                .extracting(Contact::getContactName)
                // 모두 포함되어 있고 순서는 무관한지 확인
                .containsExactlyInAnyOrder("김철수", "이영희");
    }

    // ── 테스트 3: 선택 필드 없이 저장 ───────────────────────────
    // 선택 필드(직급, 이메일, 전화)없이 연락처를 저장하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("선택 필드 없이 연락처 저장 (position, email, tel null 허용)")
    // 테스트 메서드
    void save_withoutOptionalFields() {
        // 필수 필드만 설정하여 연락처 생성
        Contact contact = Contact.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 작성자 ID 설정
                .writerId(10L)
                // 연락처 이름만 설정 (필수 필드)
                .contactName("이름만")
                // 빌드 완료 (선택 필드는 미설정)
                .build();

        // 연락처 저장
        Contact saved = contactRepository.save(contact);

        // 저장된 ID로 조회하여 연락처 반환
        Contact found = contactRepository.findById(saved.getContactId()).orElseThrow();
        // 직급이 null인지 확인
        assertThat(found.getContactPosition()).isNull();
        // 이메일이 null인지 확인
        assertThat(found.getContactEmail()).isNull();
        // 전화번호가 null인지 확인
        assertThat(found.getContactTel()).isNull();
    }

    // ── 테스트 4: createdAt 자동 설정 ───────────────────────────
    // 저장 시 생성 일시가 자동으로 설정되는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("저장 시 createdAt 자동 설정")
    // 테스트 메서드
    void save_autoSetCreatedAt() {
        // 기본 연락처 저장 (거래처ID=1, 작성자ID=10, 이름=홍길동)
        Contact saved = contactRepository.save(buildContact(1L, 10L, "홍길동"));

        // 생성 일시가 자동으로 설정되었는지 확인
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ── 테스트 5: 삭제 ─────────────────────────────────────────
    // 연락처를 삭제하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("연락처 삭제")
    // 테스트 메서드
    void deleteById() {
        // 거래처 ID=1, 작성자ID=10, 이름=삭제대상 연락처 저장
        Contact saved = contactRepository.save(buildContact(1L, 10L, "삭제대상"));

        // 저장된 연락처 ID로 삭제
        contactRepository.deleteById(saved.getContactId());

        // 삭제된 연락처가 조회되지 않는지 확인
        assertThat(contactRepository.findById(saved.getContactId()))
                // Optional이 비어있는지 확인
                .isEmpty();
    }

    // ── 테스트 6: clientId에 연락처 없을 때 빈 리스트 반환 ─────
    // 연락처가 없는 거래처 ID로 조회 시 빈 리스트를 반환하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("연락처 없는 clientId 조회 시 빈 리스트 반환")
    // 테스트 메서드
    void findAllByClientId_empty() {
        // 존재하지 않는 거래처 ID=999로 조회
        List<Contact> result = contactRepository.findAllByClientId(999L);

        // 조회 결과가 빈 리스트인지 확인
        assertThat(result).isEmpty();
    }
// ContactRepositoryTest 클래스 종료
}
