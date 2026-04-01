package com.team2.activity.command.controller;

import com.team2.activity.command.service.ActivityPackageCommandService;
import com.team2.activity.entity.ActivityPackage;
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

@WebMvcTest(ActivityPackageCommandController.class)
@WithMockUser
@DisplayName("ActivityPackageCommandController 테스트")
class ActivityPackageCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityPackageCommandService activityPackageCommandService;

    private ActivityPackage buildPackage() {
        return ActivityPackage.builder()
                .packageId(1L)
                .packageTitle("주간 패키지")
                .creatorId(10L)
                .build();
    }

    @Test
    @DisplayName("POST /api/activity-packages → 201 Created, package_id 포함")
    void createPackage_returns201() throws Exception {
        when(activityPackageCommandService.createPackage(any())).thenReturn(buildPackage());

        mockMvc.perform(post("/api/activity-packages")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "package_title": "주간 패키지",
                                    "po_id": "PO-001",
                                    "activity_ids": [1, 2, 3],
                                    "viewer_ids": [5, 6]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.package_id").exists());

        verify(activityPackageCommandService).createPackage(argThat(p -> Long.valueOf(10L).equals(p.getCreatorId())));
    }

    @Test
    @DisplayName("POST /api/activity-packages - 필수 필드 누락 → 400 Bad Request")
    void createPackage_returns400WhenRequiredFieldMissing() throws Exception {
        mockMvc.perform(post("/api/activity-packages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/activity-packages/{package_id} → 200 OK")
    void updatePackage_returns200() throws Exception {
        when(activityPackageCommandService.updateAll(eq(1L), any())).thenReturn(buildPackage());

        mockMvc.perform(put("/api/activity-packages/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "package_title": "월간 패키지",
                                    "activity_ids": [1, 2],
                                    "viewer_ids": [5]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.package_id").exists());
    }

    @Test
    @DisplayName("PUT /api/activity-packages/{package_id} - 존재하지 않는 ID → 404 Not Found")
    void updatePackage_returns404WhenNotFound() throws Exception {
        when(activityPackageCommandService.updateAll(eq(999L), any()))
                .thenThrow(new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."));

        mockMvc.perform(put("/api/activity-packages/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "package_title": "월간 패키지",
                                    "activity_ids": [1],
                                    "viewer_ids": [5]
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/activity-packages/{package_id} → 204 No Content")
    void deletePackage_returns204() throws Exception {
        doNothing().when(activityPackageCommandService).deletePackage(1L);

        mockMvc.perform(delete("/api/activity-packages/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/activity-packages/{package_id} - 존재하지 않는 ID → 404 Not Found")
    void deletePackage_returns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."))
                .when(activityPackageCommandService).deletePackage(999L);

        mockMvc.perform(delete("/api/activity-packages/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
