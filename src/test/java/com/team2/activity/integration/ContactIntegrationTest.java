package com.team2.activity.integration;

import com.team2.activity.command.domain.repository.ContactRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Contact API가 생성부터 삭제까지 전체 계층에서 동작하는지 검증한다.
@DisplayName("Contact 통합 테스트")
class ContactIntegrationTest extends IntegrationTestSupport {

    // 삭제 이후 DB 상태를 직접 검증할 repository다.
    @Autowired
    private ContactRepository contactRepository;

    // MyBatis 1차 캐시를 초기화할 때 사용할 SqlSessionTemplate다.
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

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

        // JPA INSERT를 MyBatis SELECT 전에 DB에 반영한다.
        contactRepository.flush();

        // 생성된 연락처가 거래처별 목록 조회에 노출되는지 확인한다.
        mockMvc.perform(get("/api/clients/{clientId}/contacts", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.contact_response_list[0].contact_id").value(contactId))
                .andExpect(jsonPath("$._embedded.contact_response_list[0].contact_name").value("김철수"));

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
        // MyBatis 1차 캐시를 비워 다음 SELECT가 DB에서 최신 값을 읽도록 한다.
        sqlSessionTemplate.clearCache();

        // 공통 목록 API에서도 수정된 이메일이 보이는지 확인한다.
        mockMvc.perform(get("/api/contacts").param("clientId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.contact_response_list[0].contact_id").value(contactId))
                .andExpect(jsonPath("$._embedded.contact_response_list[0].contact_email").value("park@example.com"));

        // 삭제 요청이 정상 처리되는지 확인한다.
        mockMvc.perform(delete("/api/contacts/{contactId}", contactId).with(csrf()))
                .andExpect(status().isNoContent());

        // 삭제 결과를 DB에 즉시 반영한다.
        contactRepository.flush();

        // 최종적으로 DB에서 연락처가 제거됐는지 확인한다.
        assertThat(contactRepository.findById(contactId)).isEmpty();
    }

    @Test
    @DisplayName("필수 필드 없이 연락처 생성 시 400을 반환한다")
    void createContact_returns400WhenNameMissing() throws Exception {
        // contact_name은 @NotBlank 필수 필드이므로 누락 시 400이어야 한다.
        mockMvc.perform(post("/api/clients/{clientId}/contacts", 1L)
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        // contact_name을 포함하지 않는 요청을 전송한다.
                        .content("""
                                {
                                  "contact_position": "과장",
                                  "contact_email": "kim@example.com"
                                }
                                """))
                // 유효성 검증 실패로 400 Bad Request가 반환되는지 확인한다.
                .andExpect(status().isBadRequest())
                // 응답 본문에 message 필드가 포함되는지 확인한다.
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("존재하지 않는 연락처 수정 시 404를 반환한다")
    void updateContact_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 수정 시도 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(put("/api/contacts/{contactId}", 99999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        // 유효한 수정 요청 본문을 전송해 존재 여부에서 실패하도록 한다.
                        .content("""
                                {
                                  "contact_name": "없는 담당자"
                                }
                                """))
                // 연락처 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("연락처를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 연락처 삭제 시 404를 반환한다")
    void deleteContact_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 삭제 시도 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(delete("/api/contacts/{contactId}", 99999L).with(csrf()))
                // 연락처 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("연락처를 찾을 수 없습니다."));
    }
}
