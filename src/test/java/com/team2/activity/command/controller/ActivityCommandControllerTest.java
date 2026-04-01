package com.team2.activity.command.controller;

import com.team2.activity.command.service.ActivityCommandService;
import com.team2.activity.entity.Activity;
import com.team2.activity.entity.enums.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityCommandController.class)
@WithMockUser
@DisplayName("ActivityCommandController 테스트")
class ActivityCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityCommandService activityCommandService;

    private Activity buildActivity() {
        return Activity.builder()
                .activityId(1L)
                .clientId(1L)
                .activityAuthorId(10L)
                .activityType(ActivityType.MEETING)
                .activityTitle("초기 미팅")
                .activityDate(LocalDate.of(2025, 4, 1))
                .build();
    }

    @Test
    @DisplayName("POST /api/activities → 201 Created, activity_id 포함")
    void createActivity_returns201() throws Exception {
        when(activityCommandService.createActivity(any())).thenReturn(buildActivity());

        mockMvc.perform(post("/api/activities")
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
                .andExpect(jsonPath("$.activity_id").exists());

        verify(activityCommandService).createActivity(argThat(a -> Long.valueOf(10L).equals(a.getActivityAuthorId())));
    }

    @Test
    @DisplayName("POST /api/activities - 필수 필드 누락 → 400 Bad Request")
    void createActivity_returns400WhenRequiredFieldMissing() throws Exception {
        mockMvc.perform(post("/api/activities")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "activity_title": "제목만 있음"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/activities/{activity_id} → 200 OK")
    void updateActivity_returns200() throws Exception {
        when(activityCommandService.updateActivity(eq(1L), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(buildActivity());

        mockMvc.perform(put("/api/activities/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "activity_date": "2025-04-01",
                                    "activity_type": "ISSUE",
                                    "activity_title": "긴급 이슈",
                                    "activity_content": null,
                                    "po_id": null,
                                    "activity_priority": null,
                                    "activity_schedule_from": null,
                                    "activity_schedule_to": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity_id").exists());
    }

    @Test
    @DisplayName("PUT /api/activities/{activity_id} - 존재하지 않는 ID → 404 Not Found")
    void updateActivity_returns404WhenNotFound() throws Exception {
        when(activityCommandService.updateActivity(eq(999L), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("활동을 찾을 수 없습니다."));

        mockMvc.perform(put("/api/activities/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "activity_date": "2025-04-01",
                                    "activity_type": "ISSUE",
                                    "activity_title": "긴급 이슈",
                                    "activity_content": null,
                                    "po_id": null,
                                    "activity_priority": null,
                                    "activity_schedule_from": null,
                                    "activity_schedule_to": null
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/activities/{activity_id} → 204 No Content")
    void deleteActivity_returns204() throws Exception {
        doNothing().when(activityCommandService).deleteActivity(1L);

        mockMvc.perform(delete("/api/activities/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/activities/{activity_id} - 존재하지 않는 ID → 404 Not Found")
    void deleteActivity_returns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("활동을 찾을 수 없습니다."))
                .when(activityCommandService).deleteActivity(999L);

        mockMvc.perform(delete("/api/activities/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
