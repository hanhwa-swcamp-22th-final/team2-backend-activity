package com.team2.activity.command.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// Contact 엔티티의 생성, 수정, JPA 영속화 동작을 검증한다.
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Contact 엔티티 테스트")
class ContactTest {

    // 엔티티 저장과 재조회에 사용할 JPA 테스트 전용 EntityManager다.
    @Autowired
    private TestEntityManager em;

    // 공통 Contact 픽스처를 생성한다.
    private Contact buildBasicContact() {
        return Contact.builder()
                .clientId(1L)
                .writerId(10L)
                .contactName("김철수")
                .contactPosition("Team Leader")
                .contactEmail("kim@example.com")
                .contactTel("010-1234-5678")
                .build();
    }

    @Test
    @DisplayName("기본 Contact 생성 성공")
    void createContact_basic() {
        // 필수/선택 필드를 모두 가진 연락처를 생성한다.
        Contact contact = buildBasicContact();

        // 생성 직후 필드 값이 그대로 유지되는지 확인한다.
        assertThat(contact.getClientId()).isEqualTo(1L);
        assertThat(contact.getWriterId()).isEqualTo(10L);
        assertThat(contact.getContactName()).isEqualTo("김철수");
        assertThat(contact.getContactPosition()).isEqualTo("Team Leader");
        assertThat(contact.getContactEmail()).isEqualTo("kim@example.com");
        assertThat(contact.getContactTel()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("선택 필드 없이 Contact 생성")
    void createContact_withoutOptionalFields() {
        // 선택 필드를 생략한 연락처를 생성한다.
        Contact contact = Contact.builder()
                .clientId(2L)
                .writerId(10L)
                .contactName("이영수")
                .build();

        // 선택 필드는 null로 유지될 수 있어야 한다.
        assertThat(contact.getContactPosition()).isNull();
        assertThat(contact.getContactEmail()).isNull();
        assertThat(contact.getContactTel()).isNull();
    }

    @Test
    @DisplayName("Contact 수정 - 모든 수정 가능 필드 변경")
    void updateContact_allFields() {
        // 기본 연락처에 수정 메서드를 적용한다.
        Contact contact = buildBasicContact();

        contact.update(
                "박영희",
                "Team Member",
                "park@example.com",
                "010-9999-8888"
        );

        // 수정 가능한 필드가 모두 새 값으로 바뀌는지 확인한다.
        assertThat(contact.getContactName()).isEqualTo("박영희");
        assertThat(contact.getContactPosition()).isEqualTo("Team Member");
        assertThat(contact.getContactEmail()).isEqualTo("park@example.com");
        assertThat(contact.getContactTel()).isEqualTo("010-9999-8888");
    }

    @Test
    @DisplayName("clientId는 수정 불가")
    void clientId_isImmutable() {
        // 수정 이후에도 clientId는 고정돼야 한다.
        Contact contact = buildBasicContact();

        contact.update("다른이름", null, "other@example.com", null);

        assertThat(contact.getClientId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Contact 수정 - 선택 필드 제거 (null로 클리어)")
    void updateContact_clearOptionalFields() {
        // 선택 필드를 null로 덮어써 값이 제거되는지 확인한다.
        Contact contact = buildBasicContact();

        contact.update(
                "김철수",
                null,
                "kim@example.com",
                null
        );

        assertThat(contact.getContactPosition()).isNull();
        assertThat(contact.getContactTel()).isNull();
    }

    @Test
    @DisplayName("Team Member 직위 설정")
    void createContact_teamMember() {
        // 특정 직위 문자열도 그대로 보존되는지 확인한다.
        Contact contact = Contact.builder()
                .clientId(1L)
                .writerId(10L)
                .contactName("신입직원")
                .contactPosition("Team Member")
                .build();

        assertThat(contact.getContactPosition()).isEqualTo("Team Member");
    }

    @Test
    @DisplayName("DB - 기본 Contact 저장 및 조회")
    void db_saveAndFind() {
        // 기본 연락처를 저장하고 다시 조회한다.
        Contact contact = buildBasicContact();

        Contact saved = em.persistFlushFind(contact);

        // PK와 감사 필드가 정상 생성되는지 확인한다.
        assertThat(saved.getContactId()).isNotNull();
        assertThat(saved.getContactName()).isEqualTo("김철수");
        assertThat(saved.getClientId()).isEqualTo(1L);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("DB - 선택 필드 null 저장 허용")
    void db_saveWithNullOptionalFields() {
        // 선택 필드가 null인 연락처를 저장한다.
        Contact contact = Contact.builder()
                .clientId(1L)
                .writerId(10L)
                .contactName("이름만")
                .build();

        Contact saved = em.persistFlushFind(contact);

        // DB 저장 이후에도 null 값이 유지되는지 확인한다.
        assertThat(saved.getContactPosition()).isNull();
        assertThat(saved.getContactEmail()).isNull();
        assertThat(saved.getContactTel()).isNull();
    }

    @Test
    @DisplayName("DB - update() 후 변경사항 DB 반영 확인")
    void db_updatePersists() {
        // 저장된 연락처를 수정한 뒤 영속성 컨텍스트를 비운다.
        Contact contact = buildBasicContact();
        Contact saved = em.persistAndFlush(contact);

        saved.update("박영희", "Team Member", "park@example.com", "010-9999-8888");
        em.flush();
        em.clear();

        // DB 재조회 결과에 수정 내용이 반영됐는지 확인한다.
        Contact found = em.find(Contact.class, saved.getContactId());
        assertThat(found.getContactName()).isEqualTo("박영희");
        assertThat(found.getContactPosition()).isEqualTo("Team Member");
    }
}
