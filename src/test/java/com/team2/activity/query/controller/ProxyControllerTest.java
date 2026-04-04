package com.team2.activity.query.controller;

import com.team2.activity.command.infrastructure.client.ClientResponse;
import com.team2.activity.command.infrastructure.client.MasterFeignClient;
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

@WebMvcTest(ProxyController.class)
@WithMockUser
@DisplayName("ProxyController 테스트")
class ProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MasterFeignClient masterFeignClient;

    @Test
    @DisplayName("GET /api/clients → 200 OK, 전체 거래처 목록 반환")
    void getClients_returns200() throws Exception {
        when(masterFeignClient.getClients()).thenReturn(List.of(
                new ClientResponse(1, "Global Steel", "글로벌 스틸", "CLI001"),
                new ClientResponse(2, "Asia Trading", "아시아 무역", "CLI002")));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].client_name").value("Global Steel"));
    }

    @Test
    @DisplayName("GET /api/clients?keyword=Steel → 영문 키워드로 필터링")
    void getClients_filtersEnglishKeyword() throws Exception {
        when(masterFeignClient.getClients()).thenReturn(List.of(
                new ClientResponse(1, "Global Steel", "글로벌 스틸", "CLI001"),
                new ClientResponse(2, "Asia Trading", "아시아 무역", "CLI002")));

        mockMvc.perform(get("/api/clients").param("keyword", "Steel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].client_name").value("Global Steel"));
    }

    @Test
    @DisplayName("GET /api/clients?keyword=스틸 → 한글 키워드로 필터링")
    void getClients_filtersKoreanKeyword() throws Exception {
        when(masterFeignClient.getClients()).thenReturn(List.of(
                new ClientResponse(1, "Global Steel", "글로벌 스틸", "CLI001"),
                new ClientResponse(2, "Asia Trading", "아시아 무역", "CLI002")));

        mockMvc.perform(get("/api/clients").param("keyword", "스틸"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/clients?keyword=steel → 대소문자 무시 필터링")
    void getClients_caseInsensitiveFilter() throws Exception {
        when(masterFeignClient.getClients()).thenReturn(List.of(
                new ClientResponse(1, "Global Steel", "글로벌 스틸", "CLI001")));

        mockMvc.perform(get("/api/clients").param("keyword", "steel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/clients?keyword=없는거래처 → 매칭 없으면 빈 배열")
    void getClients_noMatch_returnsEmpty() throws Exception {
        when(masterFeignClient.getClients()).thenReturn(List.of(
                new ClientResponse(1, "Global Steel", "글로벌 스틸", "CLI001")));

        mockMvc.perform(get("/api/clients").param("keyword", "없는거래처"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/clients → Master 서비스 장애 시 빈 배열 반환")
    void getClients_feignFail_returnsEmpty() throws Exception {
        when(masterFeignClient.getClients()).thenThrow(new RuntimeException("Master 서비스 장애"));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/pos → 200 OK, 빈 배열 반환 (documents 서비스 미구현)")
    void getPurchaseOrders_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/pos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/pos?client_id=1&date_from=2026-01-01&date_to=2026-12-31 → 파라미터 수용 후 빈 배열")
    void getPurchaseOrders_withParams_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/pos")
                        .param("client_id", "1")
                        .param("date_from", "2026-01-01")
                        .param("date_to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
