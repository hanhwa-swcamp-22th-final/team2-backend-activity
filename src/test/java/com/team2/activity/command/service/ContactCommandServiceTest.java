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

// ContactCommandService가 연락처 쓰기 로직을 repository에 위임하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("ContactCommandService 테스트")
class ContactCommandServiceTest {

    // 연락처 저장소 역할을 하는 repository 목 객체다.
    @Mock
    private ContactRepository contactRepository;

    // repository를 호출하는 연락처 command 서비스다.
    @InjectMocks
    private ContactCommandService contactCommandService;

    // 공통 연락처 픽스처를 생성한다.
    private Contact buildContact() {
        return Contact.builder()
                // 테스트용 거래처 ID를 설정한다.
                .clientId(1L)
                // 테스트용 작성자 ID를 설정한다.
                .writerId(10L)
                // 테스트용 이름을 설정한다.
                .contactName("홍길동")
                // 테스트용 직책을 설정한다.
                .contactPosition("과장")
                // 테스트용 이메일을 설정한다.
                .contactEmail("gil@example.com")
                // 테스트용 전화번호를 설정한다.
                .contactTel("010-0000-0000")
                // 공통 Contact 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("연락처 생성 시 repository save 결과를 반환한다")
    void createContact_returnsSavedContact() {
        // 저장할 연락처와 save 결과를 준비한다.
        Contact contact = buildContact();
        // repository save 호출 시 같은 연락처를 반환하도록 설정한다.
        when(contactRepository.save(contact)).thenReturn(contact);

        // 서비스가 repository save 결과를 그대로 반환하는지 확인한다.
        Contact result = contactCommandService.createContact(contact);

        // 반환 결과가 repository가 돌려준 연락처와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(contact);
        // save가 정확히 한 번 호출됐는지 검증한다.
        verify(contactRepository).save(contact);
    }

    @Test
    @DisplayName("연락처 수정 시 조회한 엔티티의 필드를 변경한다")
    void updateContact_updatesLoadedEntity() {
        // 수정 대상 연락처를 조회하도록 설정한다.
        Contact contact = buildContact();
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        // 이름, 직급, 이메일, 전화번호가 모두 변경되는지 확인한다.
        Contact result = contactCommandService.updateContact(
                1L,
                "김영희",
                "부장",
                "younghee@example.com",
                "010-1111-2222"
        );

        // 반환 결과가 조회한 기존 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(contact);
        // 이름이 새 값으로 바뀌었는지 확인한다.
        assertThat(contact.getContactName()).isEqualTo("김영희");
        // 직책이 새 값으로 바뀌었는지 확인한다.
        assertThat(contact.getContactPosition()).isEqualTo("부장");
        // 이메일이 새 값으로 바뀌었는지 확인한다.
        assertThat(contact.getContactEmail()).isEqualTo("younghee@example.com");
        // 전화번호가 새 값으로 바뀌었는지 확인한다.
        assertThat(contact.getContactTel()).isEqualTo("010-1111-2222");
        // 수정 전에 findById가 호출됐는지 검증한다.
        verify(contactRepository).findById(1L);
    }

    @Test
    @DisplayName("수정 대상 연락처가 없으면 예외를 던진다")
    void updateContact_throwsWhenContactDoesNotExist() {
        // 조회 결과가 없으면 수정 요청은 예외여야 한다.
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        // 없는 연락처 수정 시 IllegalArgumentException이 발생하는지 확인한다.
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
        // 삭제 대상 연락처를 repository가 조회하도록 설정한다.
        Contact contact = buildContact();
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        // 서비스가 조회 후 delete를 호출하는지 확인한다.
        contactCommandService.deleteContact(1L);

        // 삭제 전에 findById가 호출됐는지 검증한다.
        verify(contactRepository).findById(1L);
        // 조회된 연락처가 delete 대상으로 전달됐는지 검증한다.
        verify(contactRepository).delete(contact);
    }
}
