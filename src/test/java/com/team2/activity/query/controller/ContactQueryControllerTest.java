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

// Contact 조회 API가 리스트 응답을 올바르게 반환하는지 검증한다.
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
    @DisplayName("GET /api/contacts → 200 OK, 전체 목록 반환")
    void getContacts_returns200() throws Exception {
        // 전체 연락처 목록 응답을 서비스 목 객체에 등록한다.
        when(contactQueryService.getAllContacts()).thenReturn(List.of(buildContact()));

        // 기본 목록 응답이 배열 구조를 유지하는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                // 응답 본문이 배열인지 확인한다.
                .andExpect(jsonPath("$").isArray())
                // 첫 원소에 contact_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$[0].contact_id").exists())
                // 첫 원소에 contact_name이 포함되는지 확인한다.
                .andExpect(jsonPath("$[0].contact_name").exists());
    }

    @Test
    @DisplayName("GET /api/contacts?client_id=1 → 200 OK, client_id 필터 적용")
    void getContacts_returns200WithClientIdFilter() throws Exception {
        // client_id 조건 조회 결과를 서비스가 반환하도록 설정한다.
        when(contactQueryService.getContactsByClientId(1L)).thenReturn(List.of(buildContact()));

        // 필터가 있는 목록 요청도 배열 응답으로 처리되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/contacts").param("clientId", "1"))
                .andExpect(status().isOk())
                // 응답 본문이 배열인지 확인한다.
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/clients/{client_id}/contacts → 200 OK, 해당 거래처 연락처 목록")
    void getContactsByClientId_returns200() throws Exception {
        // 거래처 하위 리소스 조회 결과를 서비스가 반환하도록 설정한다.
        when(contactQueryService.getContactsByClientId(1L)).thenReturn(List.of(buildContact()));

        // 거래처별 연락처 목록 엔드포인트가 정상 응답하는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/clients/1/contacts"))
                .andExpect(status().isOk())
                // 응답 본문이 배열인지 확인한다.
                .andExpect(jsonPath("$").isArray())
                // 첫 원소에 contact_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$[0].contact_id").exists())
                // 첫 원소에 writer_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$[0].writer_id").exists());
    }
}
