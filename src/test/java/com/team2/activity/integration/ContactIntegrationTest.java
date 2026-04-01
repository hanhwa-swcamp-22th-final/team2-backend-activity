package com.team2.activity.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.activity.command.domain.repository.ContactRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Contact API가 생성부터 삭제까지 전체 계층에서 동작하는지 검증한다.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
@ActiveProfiles("test")
@DisplayName("Contact 통합 테스트")
class ContactIntegrationTest {

    // 실제 HTTP 요청처럼 연락처 API를 호출하는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 응답 JSON에서 contact_id를 추출할 ObjectMapper다.
    @Autowired
    private ObjectMapper objectMapper;

    // 삭제 이후 DB 상태를 직접 검증할 repository다.
    @Autowired
    private ContactRepository contactRepository;

    @Test
    @DisplayName("연락처 생성 후 목록 조회, 수정, 삭제까지 통합 흐름을 검증한다")
    void contactCrudFlow() throws Exception {
        // 연락처 생성 요청을 보내고 생성된 contact_id를 추출한다.
        MvcResult createResult = mockMvc.perform(post("/api/clients/{clientId}/contacts", 1L)
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contact_name": "김철수",
                                  "contact_position": "과장",
                                  "contact_email": "kim@example.com",
                                  "contact_tel": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isCreated())
                // 생성 응답에 contact_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.contact_id").exists())
                // 생성 응답에 client_id가 경로 값으로 반영됐는지 확인한다.
                .andExpect(jsonPath("$.client_id").value(1))
                // 생성 응답에 writer_id가 헤더 값으로 반영됐는지 확인한다.
                .andExpect(jsonPath("$.writer_id").value(10))
                .andReturn();

        // 후속 요청에서 사용할 contact_id를 응답에서 읽어 온다.
        long contactId = extractLong(createResult, "contact_id");

        // 생성된 연락처가 거래처별 목록 조회에 노출되는지 확인한다.
        mockMvc.perform(get("/api/clients/{clientId}/contacts", 1L))
                .andExpect(status().isOk())
                // 목록 첫 원소의 contact_id가 생성한 ID와 같은지 확인한다.
                .andExpect(jsonPath("$[0].contact_id").value(contactId))
                // 목록 첫 원소의 이름이 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$[0].contact_name").value("김철수"));

        // 수정 요청을 통해 연락처 정보가 갱신되는지 확인한다.
        mockMvc.perform(put("/api/contacts/{contactId}", contactId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contact_name": "박영희",
                                  "contact_position": "부장",
                                  "contact_email": "park@example.com",
                                  "contact_tel": "010-9999-8888"
                                }
                                """))
                .andExpect(status().isOk())
                // 수정 응답의 contact_id가 기존 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.contact_id").value(contactId))
                // 수정 응답의 이름이 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.contact_name").value("박영희"))
                // 수정 응답의 직책이 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.contact_position").value("부장"));

        // 변경된 연락처 상태를 DB에 즉시 반영한다.
        contactRepository.flush();

        // 공통 목록 API에서도 수정된 이메일이 보이는지 확인한다.
        mockMvc.perform(get("/api/contacts").param("client_id", "1"))
                .andExpect(status().isOk())
                // 목록 첫 원소의 contact_id가 수정한 연락처 ID와 같은지 확인한다.
                .andExpect(jsonPath("$[0].contact_id").value(contactId))
                // 목록 첫 원소의 이메일이 수정 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$[0].contact_email").value("park@example.com"));

        // 삭제 요청이 정상 처리되는지 확인한다.
        mockMvc.perform(delete("/api/contacts/{contactId}", contactId).with(csrf()))
                .andExpect(status().isNoContent());

        // 삭제 결과를 DB에 즉시 반영한다.
        contactRepository.flush();

        // 최종적으로 DB에서 연락처가 제거됐는지 확인한다.
        assertThat(contactRepository.findById(contactId)).isEmpty();
    }

    // JSON 응답 본문에서 지정한 숫자 필드를 추출한다.
    private long extractLong(MvcResult result, String fieldName) throws Exception {
        // 응답 본문 문자열을 JSON 트리로 파싱한다.
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        // 지정한 필드의 숫자 값을 long으로 꺼낸다.
        return jsonNode.get(fieldName).asLong();
    }
}
