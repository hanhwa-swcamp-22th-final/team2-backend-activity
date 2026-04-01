package com.team2.activity.query.controller;

import com.team2.activity.entity.ActivityPackage;
import com.team2.activity.query.service.ActivityPackageQueryService;
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

@WebMvcTest(ActivityPackageQueryController.class)
@WithMockUser
@DisplayName("ActivityPackageQueryController 테스트")
class ActivityPackageQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityPackageQueryService activityPackageQueryService;

    private ActivityPackage buildPackage() {
        return ActivityPackage.builder()
                .packageId(1L)
                .packageTitle("주간 패키지")
                .creatorId(10L)
                .build();
    }

    @Test
    @DisplayName("GET /api/activity-packages → 200 OK, 페이징 응답 구조 포함")
    void getPackages_returns200WithPagedResult() throws Exception {
        when(activityPackageQueryService.getAllPackages()).thenReturn(List.of(buildPackage()));

        mockMvc.perform(get("/api/activity-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.total_elements").exists())
                .andExpect(jsonPath("$.total_pages").exists())
                .andExpect(jsonPath("$.current_page").exists());
    }

    @Test
    @DisplayName("GET /api/activity-packages?creator_id=10 → 200 OK, creator_id 필터 적용")
    void getPackages_returns200WithCreatorIdFilter() throws Exception {
        when(activityPackageQueryService.getPackagesByCreatorId(10L)).thenReturn(List.of(buildPackage()));

        mockMvc.perform(get("/api/activity-packages").param("creator_id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/activity-packages/{package_id} → 200 OK, 상세 필드 포함")
    void getPackage_returns200WithDetail() throws Exception {
        when(activityPackageQueryService.getPackage(1L)).thenReturn(buildPackage());

        mockMvc.perform(get("/api/activity-packages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.package_id").exists())
                .andExpect(jsonPath("$.package_title").exists())
                .andExpect(jsonPath("$.activity_ids").isArray())
                .andExpect(jsonPath("$.viewer_ids").isArray());
    }

    @Test
    @DisplayName("GET /api/activity-packages/{package_id} - 존재하지 않는 ID → 404 Not Found")
    void getPackage_returns404WhenNotFound() throws Exception {
        when(activityPackageQueryService.getPackage(999L))
                .thenThrow(new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/activity-packages/999"))
                .andExpect(status().isNotFound());
    }
}
