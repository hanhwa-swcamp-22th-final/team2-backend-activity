package com.team2.activity.command.controller;

import com.team2.activity.command.service.ContactCommandService;
import com.team2.activity.entity.Contact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactCommandController.class)
@WithMockUser
@DisplayName("ContactCommandController 테스트")
class ContactCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactCommandService contactCommandService;

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
    @DisplayName("POST /api/clients/{client_id}/contacts → 201 Created, contact_id 포함")
    void createContact_returns201() throws Exception {
        when(contactCommandService.createContact(any())).thenReturn(buildContact());

        mockMvc.perform(post("/api/clients/1/contacts")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "contact_name": "김담당",
                                    "contact_position": "과장",
                                    "contact_email": "kim@example.com",
                                    "contact_tel": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contact_id").exists());

        verify(contactCommandService).createContact(argThat(c -> Long.valueOf(10L).equals(c.getWriterId())));
    }

    @Test
    @DisplayName("POST /api/clients/{client_id}/contacts - 필수 필드 누락 → 400 Bad Request")
    void createContact_returns400WhenRequiredFieldMissing() throws Exception {
        mockMvc.perform(post("/api/clients/1/contacts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/contacts/{contact_id} → 200 OK")
    void updateContact_returns200() throws Exception {
        when(contactCommandService.updateContact(eq(1L), any(), any(), any(), any()))
                .thenReturn(buildContact());

        mockMvc.perform(put("/api/contacts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "contact_name": "이담당",
                                    "contact_position": "부장",
                                    "contact_email": "lee@example.com",
                                    "contact_tel": "010-9999-0000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contact_id").exists());
    }

    @Test
    @DisplayName("PUT /api/contacts/{contact_id} - 존재하지 않는 ID → 404 Not Found")
    void updateContact_returns404WhenNotFound() throws Exception {
        when(contactCommandService.updateContact(eq(999L), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("연락처를 찾을 수 없습니다."));

        mockMvc.perform(put("/api/contacts/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "contact_name": "이담당",
                                    "contact_position": "부장",
                                    "contact_email": "lee@example.com",
                                    "contact_tel": "010-9999-0000"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/contacts/{contact_id} → 204 No Content")
    void deleteContact_returns204() throws Exception {
        doNothing().when(contactCommandService).deleteContact(1L);

        mockMvc.perform(delete("/api/contacts/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/contacts/{contact_id} - 존재하지 않는 ID → 404 Not Found")
    void deleteContact_returns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("연락처를 찾을 수 없습니다."))
                .when(contactCommandService).deleteContact(999L);

        mockMvc.perform(delete("/api/contacts/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
