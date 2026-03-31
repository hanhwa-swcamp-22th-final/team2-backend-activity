package com.team2.activity.query.entity;

import com.team2.activity.entity.Contact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Query Contact 엔티티 테스트")
class ContactQueryEntityTest {

    @Test
    @DisplayName("조회용 Contact는 기본 필드를 그대로 가진다")
    void createReadContact_basicFields() {
        Contact contact = Contact.builder()
                .clientId(1L)
                .writerId(10L)
                .contactName("김철수")
                .contactPosition("과장")
                .contactEmail("kim@example.com")
                .contactTel("010-1234-5678")
                .build();

        assertThat(contact.getClientId()).isEqualTo(1L);
        assertThat(contact.getWriterId()).isEqualTo(10L);
        assertThat(contact.getContactName()).isEqualTo("김철수");
        assertThat(contact.getContactPosition()).isEqualTo("과장");
        assertThat(contact.getContactEmail()).isEqualTo("kim@example.com");
        assertThat(contact.getContactTel()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("조회용 Contact는 선택 필드가 비어 있을 수 있다")
    void createReadContact_withoutOptionalFields() {
        Contact contact = Contact.builder()
                .clientId(2L)
                .writerId(11L)
                .contactName("이영수")
                .build();

        assertThat(contact.getContactPosition()).isNull();
        assertThat(contact.getContactEmail()).isNull();
        assertThat(contact.getContactTel()).isNull();
    }
}
