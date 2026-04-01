package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.Contact;
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

// ContactQueryService가 읽기 전용 조회를 mapper에 위임하는지 검증한다.
@ExtendWith(MockitoExtension.class)
@DisplayName("ContactQueryService 테스트")
class ContactQueryServiceTest {

    // 연락처 조회 SQL을 수행하는 mapper 목 객체다.
    @Mock
    private ContactQueryMapper contactQueryMapper;

    // mapper를 호출하는 연락처 조회 서비스다.
    @InjectMocks
    private ContactQueryService contactQueryService;

    // 연락처 조회 테스트에 사용할 공통 Contact 객체를 생성한다.
    private Contact buildContact(Long clientId, String name) {
        return Contact.builder()
                // 테스트용 거래처 ID를 설정한다.
                .clientId(clientId)
                // 테스트용 작성자 ID를 설정한다.
                .writerId(10L)
                // 테스트용 이름을 설정한다.
                .contactName(name)
                // 테스트용 직책을 설정한다.
                .contactPosition("과장")
                // 테스트용 이메일을 설정한다.
                .contactEmail(name + "@example.com")
                // 테스트용 전화번호를 설정한다.
                .contactTel("010-0000-0000")
                // 공통 Contact 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("단건 조회 시 mapper 결과를 반환한다")
    void getContact_returnsMappedContact() {
        // mapper가 반환할 연락처 엔티티를 준비한다.
        Contact contact = buildContact(1L, "hong");
        // mapper findById 호출 시 같은 엔티티를 반환하도록 설정한다.
        when(contactQueryMapper.findById(1L)).thenReturn(contact);

        // 서비스가 mapper 결과를 그대로 반환하는지 확인한다.
        Contact result = contactQueryService.getContact(1L);

        // 반환 결과가 mapper가 돌려준 엔티티와 같은 객체인지 확인한다.
        assertThat(result).isSameAs(contact);
        // findById가 정확히 한 번 호출됐는지 검증한다.
        verify(contactQueryMapper).findById(1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 예외를 던진다")
    void getContact_throwsWhenContactDoesNotExist() {
        // mapper가 null을 반환하면 서비스가 조회 실패 예외를 던져야 한다.
        when(contactQueryMapper.findById(999L)).thenReturn(null);

        // 없는 연락처 조회 시 IllegalArgumentException이 발생하는지 확인한다.
        assertThatThrownBy(() -> contactQueryService.getContact(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("연락처를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 조회 시 mapper 목록을 그대로 반환한다")
    void getAllContacts_returnsMapperResult() {
        // mapper가 반환할 연락처 목록을 준비한다.
        List<Contact> contacts = List.of(
                buildContact(1L, "hong"),
                buildContact(2L, "kim")
        );
        // mapper findAll 호출 시 준비한 목록을 반환하도록 설정한다.
        when(contactQueryMapper.findAll()).thenReturn(contacts);

        // 서비스가 조회 결과를 가공 없이 전달하는지 확인한다.
        List<Contact> result = contactQueryService.getAllContacts();

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(contacts);
        // findAll이 정확히 한 번 호출됐는지 검증한다.
        verify(contactQueryMapper).findAll();
    }

    @Test
    @DisplayName("거래처 ID 조건 조회를 위임한다")
    void getContactsByClientId_delegatesToMapper() {
        // 특정 거래처의 연락처 목록을 mapper가 반환하도록 설정한다.
        List<Contact> contacts = List.of(buildContact(1L, "hong"));
        // mapper가 거래처 조건 목록을 반환하도록 설정한다.
        when(contactQueryMapper.findAllByClientId(1L)).thenReturn(contacts);

        // 서비스가 clientId 조건 조회를 mapper에 위임하는지 확인한다.
        List<Contact> result = contactQueryService.getContactsByClientId(1L);

        // 반환 결과가 mapper가 돌려준 목록과 같은지 확인한다.
        assertThat(result).isEqualTo(contacts);
        // findAllByClientId가 정확히 한 번 호출됐는지 검증한다.
        verify(contactQueryMapper).findAllByClientId(1L);
    }
}
