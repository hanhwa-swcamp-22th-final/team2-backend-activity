package com.team2.activity.command.service;

import com.team2.activity.command.repository.ContactRepository;
import com.team2.activity.entity.Contact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactCommandService 테스트")
class ContactCommandServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactCommandService contactCommandService;

    private Contact buildContact() {
        return Contact.builder()
                .clientId(1L)
                .writerId(10L)
                .contactName("홍길동")
                .contactPosition("과장")
                .contactEmail("gil@example.com")
                .contactTel("010-0000-0000")
                .build();
    }

    @Test
    @DisplayName("연락처 생성 시 repository save 결과를 반환한다")
    void createContact_returnsSavedContact() {
        Contact contact = buildContact();
        when(contactRepository.save(contact)).thenReturn(contact);

        Contact result = contactCommandService.createContact(contact);

        assertThat(result).isSameAs(contact);
        verify(contactRepository).save(contact);
    }

    @Test
    @DisplayName("연락처 수정 시 조회한 엔티티의 필드를 변경한다")
    void updateContact_updatesLoadedEntity() {
        Contact contact = buildContact();
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        Contact result = contactCommandService.updateContact(
                1L,
                "김영희",
                "부장",
                "younghee@example.com",
                "010-1111-2222"
        );

        assertThat(result).isSameAs(contact);
        assertThat(contact.getContactName()).isEqualTo("김영희");
        assertThat(contact.getContactPosition()).isEqualTo("부장");
        assertThat(contact.getContactEmail()).isEqualTo("younghee@example.com");
        assertThat(contact.getContactTel()).isEqualTo("010-1111-2222");
        verify(contactRepository).findById(1L);
    }

    @Test
    @DisplayName("수정 대상 연락처가 없으면 예외를 던진다")
    void updateContact_throwsWhenContactDoesNotExist() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactCommandService.updateContact(
                999L,
                "이름",
                "직급",
                "email@example.com",
                "010-9999-9999"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("연락처를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("연락처 삭제 시 조회한 엔티티를 삭제한다")
    void deleteContact_deletesLoadedEntity() {
        Contact contact = buildContact();
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        contactCommandService.deleteContact(1L);

        verify(contactRepository).findById(1L);
        verify(contactRepository).delete(contact);
    }
}
