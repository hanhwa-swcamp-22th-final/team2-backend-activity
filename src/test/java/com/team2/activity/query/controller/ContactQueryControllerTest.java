package com.team2.activity.query.controller;

import com.team2.activity.entity.Contact;
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

@WebMvcTest(ContactQueryController.class)
@WithMockUser
@DisplayName("ContactQueryController 테스트")
class ContactQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactQueryService contactQueryService;

    private Contact buildContact() {
        return Contact.builder()
                .contactId(1L)
                .clientId(1L)
                .writerId(10L)
                .contactName("김담당")
                .contactEmail("kim@example.com")
                .build();
    }

    @Test
    @DisplayName("GET /api/contacts → 200 OK, 전체 목록 반환")
    void getContacts_returns200() throws Exception {
        when(contactQueryService.getAllContacts()).thenReturn(List.of(buildContact()));

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].contact_id").exists())
                .andExpect(jsonPath("$[0].contact_name").exists());
    }

    @Test
    @DisplayName("GET /api/contacts?client_id=1 → 200 OK, client_id 필터 적용")
    void getContacts_returns200WithClientIdFilter() throws Exception {
        when(contactQueryService.getContactsByClientId(1L)).thenReturn(List.of(buildContact()));

        mockMvc.perform(get("/api/contacts").param("client_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/clients/{client_id}/contacts → 200 OK, 해당 거래처 연락처 목록")
    void getContactsByClientId_returns200() throws Exception {
        when(contactQueryService.getContactsByClientId(1L)).thenReturn(List.of(buildContact()));

        mockMvc.perform(get("/api/clients/1/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].contact_id").exists())
                .andExpect(jsonPath("$[0].writer_id").exists());
    }
}
