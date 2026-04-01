package com.team2.activity.query.controller;

import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import com.team2.activity.query.service.ActivityQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityQueryController.class)
@WithMockUser
@DisplayName("ActivityQueryController 테스트")
class ActivityQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityQueryService activityQueryService;

    private Activity buildActivity() {
        return Activity.builder()
                .activityId(1L)
                .clientId(1L)
                .activityAuthorId(10L)
                .activityType(ActivityType.MEETING)
                .activityTitle("초기 미팅")
                .activityContent("테스트 내용")
                .activityDate(LocalDate.of(2025, 4, 1))
                .build();
    }

    @Test
    @DisplayName("GET /api/activities → 200 OK, 페이징 응답 구조 포함")
    void getActivities_returns200WithPagedResult() throws Exception {
        when(activityQueryService.getAllActivities()).thenReturn(List.of(buildActivity()));

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.total_elements").exists())
                .andExpect(jsonPath("$.total_pages").exists())
                .andExpect(jsonPath("$.current_page").exists());
    }

    @Test
    @DisplayName("GET /api/activities?client_id=1 → 200 OK, client_id 필터 적용")
    void getActivities_returns200WithClientIdFilter() throws Exception {
        when(activityQueryService.getActivitiesByClientId(1L)).thenReturn(List.of(buildActivity()));

        mockMvc.perform(get("/api/activities").param("client_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/activities/{activity_id} → 200 OK, 상세 필드 포함")
    void getActivity_returns200WithDetail() throws Exception {
        when(activityQueryService.getActivity(1L)).thenReturn(buildActivity());

        mockMvc.perform(get("/api/activities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity_id").exists())
                .andExpect(jsonPath("$.activity_type").exists())
                .andExpect(jsonPath("$.activity_title").exists())
                .andExpect(jsonPath("$.activity_content").exists());
    }

    @Test
    @DisplayName("GET /api/activities/{activity_id} - 존재하지 않는 ID → 404 Not Found")
    void getActivity_returns404WhenNotFound() throws Exception {
        when(activityQueryService.getActivity(999L))
                .thenThrow(new IllegalArgumentException("활동을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/activities/999"))
                .andExpect(status().isNotFound());
    }
}
