package com.team2.activity.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Contact 엔티티 테스트")
class ContactTest {

    @Autowired
    private TestEntityManager em;

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
        Contact contact = buildBasicContact();

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
        Contact contact = Contact.builder()
                .clientId(2L)
                .writerId(10L)
                .contactName("이영수")
                .build();

        assertThat(contact.getContactPosition()).isNull();
        assertThat(contact.getContactEmail()).isNull();
        assertThat(contact.getContactTel()).isNull();
    }

    @Test
    @DisplayName("Contact 수정 - 모든 수정 가능 필드 변경")
    void updateContact_allFields() {
        Contact contact = buildBasicContact();

        contact.update(
                "박영희",
                "Team Member",
                "park@example.com",
                "010-9999-8888"
        );

        assertThat(contact.getContactName()).isEqualTo("박영희");
        assertThat(contact.getContactPosition()).isEqualTo("Team Member");
        assertThat(contact.getContactEmail()).isEqualTo("park@example.com");
        assertThat(contact.getContactTel()).isEqualTo("010-9999-8888");
    }

    @Test
    @DisplayName("clientId는 수정 불가")
    void clientId_isImmutable() {
        Contact contact = buildBasicContact();

        contact.update("다른이름", null, "other@example.com", null);

        assertThat(contact.getClientId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Contact 수정 - 선택 필드 제거 (null로 클리어)")
    void updateContact_clearOptionalFields() {
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
        Contact contact = buildBasicContact();

        Contact saved = em.persistFlushFind(contact);

        assertThat(saved.getContactId()).isNotNull();
        assertThat(saved.getContactName()).isEqualTo("김철수");
        assertThat(saved.getClientId()).isEqualTo(1L);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("DB - 선택 필드 null 저장 허용")
    void db_saveWithNullOptionalFields() {
        Contact contact = Contact.builder()
                .clientId(1L)
                .writerId(10L)
                .contactName("이름만")
                .build();

        Contact saved = em.persistFlushFind(contact);

        assertThat(saved.getContactPosition()).isNull();
        assertThat(saved.getContactEmail()).isNull();
        assertThat(saved.getContactTel()).isNull();
    }

    @Test
    @DisplayName("DB - update() 후 변경사항 DB 반영 확인")
    void db_updatePersists() {
        Contact contact = buildBasicContact();
        Contact saved = em.persistAndFlush(contact);

        saved.update("박영희", "Team Member", "park@example.com", "010-9999-8888");
        em.flush();
        em.clear();

        Contact found = em.find(Contact.class, saved.getContactId());
        assertThat(found.getContactName()).isEqualTo("박영희");
        assertThat(found.getContactPosition()).isEqualTo("Team Member");
    }
}
