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

// Contact 쓰기 API의 생성, 수정, 삭제 응답을 검증한다.
@WebMvcTest(ContactCommandController.class)
@WithMockUser
@DisplayName("ContactCommandController 테스트")
class ContactCommandControllerTest {

    // 컨트롤러 요청을 수행하는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 의존하는 연락처 command 서비스 목 객체다.
    @MockBean
    private ContactCommandService contactCommandService;

    // 응답 검증에 사용할 공통 Contact 픽스처다.
    private Contact buildContact() {
        return Contact.builder()
                // 테스트용 연락처 ID를 설정한다.
                .contactId(1L)
                // 테스트용 거래처 ID를 설정한다.
                .clientId(1L)
                // 테스트용 작성자 ID를 설정한다.
                .writerId(10L)
                // 테스트용 연락처 이름을 설정한다.
                .contactName("김담당")
                // 테스트용 연락처 이메일을 설정한다.
                .contactEmail("kim@example.com")
                // 공통 Contact 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("POST /api/clients/{client_id}/contacts → 201 Created, contact_id 포함")
    void createContact_returns201() throws Exception {
        // 생성 요청에 대한 서비스 반환값을 준비한다.
        when(contactCommandService.createContact(any())).thenReturn(buildContact());

        // 유효한 생성 요청이 201 응답으로 처리되는지 확인한다.
        // 응답 상태가 201 Created인지 확인한다.
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
                // 응답 본문에 contact_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.contact_id").exists());

        // 헤더의 사용자 ID가 writerId로 매핑됐는지 검증한다.
        verify(contactCommandService).createContact(argThat(c -> Long.valueOf(10L).equals(c.getWriterId())));
    }

    @Test
    @DisplayName("POST /api/clients/{client_id}/contacts - 필수 필드 누락 → 400 Bad Request")
    void createContact_returns400WhenRequiredFieldMissing() throws Exception {
        // 필수 필드 누락 요청이 검증 오류로 처리되는지 확인한다.
        // 응답 상태가 400 Bad Request인지 확인한다.
        mockMvc.perform(post("/api/clients/1/contacts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/contacts/{contact_id} → 200 OK")
    void updateContact_returns200() throws Exception {
        // 수정 요청에 대한 서비스 반환값을 준비한다.
        when(contactCommandService.updateContact(eq(1L), any(), any(), any(), any()))
                .thenReturn(buildContact());

        // 유효한 수정 요청이 200 응답으로 처리되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
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
                // 응답 본문에 contact_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.contact_id").exists());
    }

    @Test
    @DisplayName("PUT /api/contacts/{contact_id} - 존재하지 않는 ID → 404 Not Found")
    void updateContact_returns404WhenNotFound() throws Exception {
        // 서비스 조회 실패 예외가 404 응답으로 변환되는지 검증한다.
        when(contactCommandService.updateContact(eq(999L), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("연락처를 찾을 수 없습니다."));

        // 응답 상태가 404 Not Found인지 확인한다.
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
        // 삭제 요청이 정상 완료되도록 서비스 목 객체를 설정한다.
        doNothing().when(contactCommandService).deleteContact(1L);

        // 정상 삭제 요청이 204 응답으로 처리되는지 확인한다.
        // 응답 상태가 204 No Content인지 확인한다.
        mockMvc.perform(delete("/api/contacts/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/contacts/{contact_id} - 존재하지 않는 ID → 404 Not Found")
    void deleteContact_returns404WhenNotFound() throws Exception {
        // 삭제 대상이 없을 때 404 응답으로 변환되는지 검증한다.
        org.mockito.Mockito.doThrow(new IllegalArgumentException("연락처를 찾을 수 없습니다."))
                .when(contactCommandService).deleteContact(999L);

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(delete("/api/contacts/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
