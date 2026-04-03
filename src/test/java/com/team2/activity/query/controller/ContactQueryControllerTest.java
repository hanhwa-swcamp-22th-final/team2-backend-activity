package com.team2.activity.query.controller;

import com.team2.activity.command.domain.entity.Contact;
import com.team2.activity.query.service.ContactQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Contact 조회 API가 올바른 응답 구조를 반환하는지 검증한다.
@WebMvcTest(ContactQueryController.class)
@WithMockUser
@DisplayName("ContactQueryController 테스트")
class ContactQueryControllerTest {

    // 컨트롤러 요청을 수행하는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 의존하는 연락처 조회 서비스 목 객체다.
    @MockBean
    private ContactQueryService contactQueryService;

    // 연락처 응답 검증에 사용할 공통 Contact 픽스처를 만든다.
    private Contact buildContact() {
        return Contact.builder()
                // 테스트용 연락처 ID를 설정한다.
                .contactId(1L)
                // 테스트용 거래처 ID를 설정한다.
                .clientId(1L)
                // 테스트용 작성자 ID를 설정한다.
                .writerId(10L)
                // 테스트용 이름을 설정한다.
                .contactName("김담당")
                // 테스트용 이메일을 설정한다.
                .contactEmail("kim@example.com")
                // 공통 Contact 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("GET /api/contacts → 200 OK, HATEOAS CollectionModel 구조로 목록 반환")
    void getContacts_returns200() throws Exception {
        when(contactQueryService.getAllContacts()).thenReturn(List.of(buildContact()));

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.contact_response_list").exists());
    }

    @Test
    @DisplayName("GET /api/contacts?clientId=1 → 200 OK, client_id 필터 적용")
    void getContacts_returns200WithClientIdFilter() throws Exception {
        when(contactQueryService.getContactsByClientId(1L)).thenReturn(List.of(buildContact()));

        mockMvc.perform(get("/api/contacts").param("clientId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.contact_response_list").exists());
    }

    @Test
    @DisplayName("GET /api/clients/{client_id}/contacts → 200 OK, 해당 거래처 연락처 반환")
    void getContactsByClientId_returns200() throws Exception {
        when(contactQueryService.getContactsByClientId(1L)).thenReturn(List.of(buildContact()));

        mockMvc.perform(get("/api/clients/1/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.contact_response_list").exists());
    }
}
