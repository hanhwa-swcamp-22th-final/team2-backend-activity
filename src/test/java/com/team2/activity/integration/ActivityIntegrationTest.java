package com.team2.activity.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.activity.command.domain.repository.ActivityRepository;
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

// Activity API가 컨트롤러부터 DB 반영까지 전체 흐름으로 동작하는지 검증한다.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
@ActiveProfiles("test")
@DisplayName("Activity 통합 테스트")
class ActivityIntegrationTest {

    // 실제 HTTP 요청처럼 API를 호출하는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 응답 JSON에서 식별자를 추출할 ObjectMapper다.
    @Autowired
    private ObjectMapper objectMapper;

    // 삭제 이후 DB 반영 여부를 직접 확인할 repository다.
    @Autowired
    private ActivityRepository activityRepository;

    @Test
    @DisplayName("활동 생성 후 조회, 수정, 삭제까지 통합 흐름을 검증한다")
    void activityCrudFlow() throws Exception {
        // 활동 생성 요청을 보내고 생성된 ID를 응답에서 추출한다.
        MvcResult createResult = mockMvc.perform(post("/api/activities")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "client_id": 1,
                                  "activity_date": "2025-04-01",
                                  "activity_type": "MEETING",
                                  "activity_title": "초기 미팅"
                                }
                                """))
                .andExpect(status().isCreated())
                // 생성 응답에 activity_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_id").exists())
                // 생성 응답에 작성자 ID가 헤더 값으로 반영됐는지 확인한다.
                .andExpect(jsonPath("$.activity_author_id").value(10))
                .andReturn();

        // 이후 요청에 사용할 activity_id를 응답에서 읽어 온다.
        long activityId = extractLong(createResult, "activity_id");

        // 생성된 활동이 상세 조회 API에서 그대로 조회되는지 확인한다.
        mockMvc.perform(get("/api/activities/{activityId}", activityId))
                .andExpect(status().isOk())
                // 상세 응답의 activity_id가 방금 생성한 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.activity_id").value(activityId))
                // 상세 응답의 활동 타입이 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.activity_type").value("MEETING"))
                // 상세 응답의 제목이 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.activity_title").value("초기 미팅"));

        // 활동 수정 요청이 실제 API 계층을 통해 반영되는지 확인한다.
        mockMvc.perform(put("/api/activities/{activityId}", activityId)
                        .with(csrf())
                        .header("X-User-Id", "99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activity_date": "2025-04-05",
                                  "activity_type": "ISSUE",
                                  "activity_title": "긴급 이슈",
                                  "activity_content": "우선 처리 필요",
                                  "po_id": "PO-2025-001",
                                  "activity_priority": "HIGH",
                                  "activity_schedule_from": "2025-04-06",
                                  "activity_schedule_to": "2025-04-07"
                                }
                                """))
                .andExpect(status().isOk())
                // 수정 응답의 activity_id가 기존 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.activity_id").value(activityId))
                // 수정 응답의 작성자 ID가 새 헤더 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.activity_author_id").value(99))
                // 수정 응답의 활동 타입이 ISSUE로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.activity_type").value("ISSUE"))
                // 수정 응답의 제목이 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.activity_title").value("긴급 이슈"))
                // 수정 응답의 우선순위가 HIGH로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.activity_priority").value("HIGH"));

        // 영속성 컨텍스트 변경사항을 DB에 즉시 반영한다.
        activityRepository.flush();

        // 목록 조회에서도 수정된 제목이 노출되는지 확인한다.
        mockMvc.perform(get("/api/activities").param("client_id", "1"))
                .andExpect(status().isOk())
                // 목록 응답 첫 원소의 activity_id가 수정한 활동 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.content[0].activity_id").value(activityId))
                // 목록 응답 첫 원소의 제목이 수정 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.content[0].activity_title").value("긴급 이슈"));

        // 삭제 요청이 정상적으로 처리되는지 확인한다.
        mockMvc.perform(delete("/api/activities/{activityId}", activityId).with(csrf()))
                .andExpect(status().isNoContent());

        // 삭제 결과를 DB 조회 전에 즉시 반영한다.
        activityRepository.flush();

        // 최종적으로 DB에서도 활동이 제거됐는지 확인한다.
        assertThat(activityRepository.findById(activityId)).isEmpty();
    }

    // JSON 응답 본문에서 지정한 숫자 필드를 추출한다.
    private long extractLong(MvcResult result, String fieldName) throws Exception {
        // 응답 본문 문자열을 JSON 트리로 파싱한다.
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        // 지정한 필드의 숫자 값을 long으로 꺼낸다.
        return jsonNode.get(fieldName).asLong();
    }
}
