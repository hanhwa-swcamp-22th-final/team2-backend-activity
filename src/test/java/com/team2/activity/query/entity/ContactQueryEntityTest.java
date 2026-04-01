package com.team2.activity.query.entity;

import com.team2.activity.entity.Contact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 조회 전용 Contact 엔티티의 필드 보존과 null 허용 여부를 검증한다.
@DisplayName("Query Contact 엔티티 테스트")
class ContactQueryEntityTest {

    @Test
    @DisplayName("조회용 Contact는 기본 필드를 그대로 가진다")
    void createReadContact_basicFields() {
        // 읽기 모델에서 반환될 연락처 엔티티를 생성한다.
        Contact contact = Contact.builder()
                .clientId(1L)
                .writerId(10L)
                .contactName("김철수")
                .contactPosition("과장")
                .contactEmail("kim@example.com")
                .contactTel("010-1234-5678")
                .build();

        // 기본 연락처 필드가 생성 값 그대로 유지되는지 확인한다.
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
        // 선택 필드를 생략한 조회 엔티티를 생성한다.
        Contact contact = Contact.builder()
                .clientId(2L)
                .writerId(11L)
                .contactName("이영수")
                .build();

        // 선택 필드는 null로 유지될 수 있어야 한다.
        assertThat(contact.getContactPosition()).isNull();
        assertThat(contact.getContactEmail()).isNull();
        assertThat(contact.getContactTel()).isNull();
    }
}
