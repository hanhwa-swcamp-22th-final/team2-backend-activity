package com.team2.activity.query.service;

import com.team2.activity.entity.Contact;
import com.team2.activity.query.mapper.ContactQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactQueryService 테스트")
class ContactQueryServiceTest {

    @Mock
    private ContactQueryMapper contactQueryMapper;

    @InjectMocks
    private ContactQueryService contactQueryService;

    private Contact buildContact(Long clientId, String name) {
        return Contact.builder()
                .clientId(clientId)
                .writerId(10L)
                .contactName(name)
                .contactPosition("과장")
                .contactEmail(name + "@example.com")
                .contactTel("010-0000-0000")
                .build();
    }

    @Test
    @DisplayName("단건 조회 시 mapper 결과를 반환한다")
    void getContact_returnsMappedContact() {
        Contact contact = buildContact(1L, "hong");
        when(contactQueryMapper.findById(1L)).thenReturn(contact);

        Contact result = contactQueryService.getContact(1L);

        assertThat(result).isSameAs(contact);
        verify(contactQueryMapper).findById(1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 예외를 던진다")
    void getContact_throwsWhenContactDoesNotExist() {
        when(contactQueryMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> contactQueryService.getContact(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("연락처를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 조회 시 mapper 목록을 그대로 반환한다")
    void getAllContacts_returnsMapperResult() {
        List<Contact> contacts = List.of(
                buildContact(1L, "hong"),
                buildContact(2L, "kim")
        );
        when(contactQueryMapper.findAll()).thenReturn(contacts);

        List<Contact> result = contactQueryService.getAllContacts();

        assertThat(result).isEqualTo(contacts);
        verify(contactQueryMapper).findAll();
    }

    @Test
    @DisplayName("거래처 ID 조건 조회를 위임한다")
    void getContactsByClientId_delegatesToMapper() {
        List<Contact> contacts = List.of(buildContact(1L, "hong"));
        when(contactQueryMapper.findAllByClientId(1L)).thenReturn(contacts);

        List<Contact> result = contactQueryService.getContactsByClientId(1L);

        assertThat(result).isEqualTo(contacts);
        verify(contactQueryMapper).findAllByClientId(1L);
    }
}
