package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ActivityCommandService;
import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
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

// Activity 쓰기 API가 요청 본문과 예외를 올바른 HTTP 응답으로 변환하는지 검증한다.
@WebMvcTest(ActivityCommandController.class)
@WithMockUser
@DisplayName("ActivityCommandController 테스트")
class ActivityCommandControllerTest {

    // 컨트롤러 요청을 수행하는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 호출할 command 서비스 목 객체다.
    @MockBean
    private ActivityCommandService activityCommandService;

    // 응답 검증에 사용할 공통 Activity 픽스처다.
    private Activity buildActivity() {
        return Activity.builder()
                // 테스트용 활동 ID를 설정한다.
                .activityId(1L)
                // 테스트용 거래처 ID를 설정한다.
                .clientId(1L)
                // 테스트용 작성자 ID를 설정한다.
                .activityAuthorId(10L)
                // 테스트용 활동 타입을 설정한다.
                .activityType(ActivityType.MEETING)
                // 테스트용 활동 제목을 설정한다.
                .activityTitle("초기 미팅")
                // 테스트용 활동 날짜를 설정한다.
                .activityDate(LocalDate.of(2025, 4, 1))
                // 공통 Activity 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("POST /api/activities → 201 Created, activity_id 포함")
    void createActivity_returns201() throws Exception {
        // 생성 요청에 대한 서비스 반환값을 준비한다.
        when(activityCommandService.createActivity(any())).thenReturn(buildActivity());

        // 유효한 생성 요청이 201 응답과 식별자를 반환하는지 확인한다.
        // 응답 상태가 201 Created인지 확인한다.
        mockMvc.perform(post("/api/activities")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "client_id": 1,
                                    "po_id": "PO-2025-001",
                                    "activity_date": "2025-04-01",
                                    "activity_type": "meeting",
                                    "activity_title": "초기 미팅",
                                    "activity_schedule_from": "2025-04-01",
                                    "activity_schedule_to": "2025-04-05"
                                }
                                """))
                .andExpect(status().isCreated())
                // 응답 본문에 activity_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_id").exists());

        // 헤더의 사용자 ID가 작성자 ID로 매핑됐는지 검증한다.
        verify(activityCommandService).createActivity(argThat(a -> Long.valueOf(10L).equals(a.getActivityAuthorId())));
    }

    @Test
    @DisplayName("POST /api/activities - 필수 필드 누락 → 400 Bad Request")
    void createActivity_returns400WhenRequiredFieldMissing() throws Exception {
        // 필수 필드 누락 요청이 검증 오류로 처리되는지 확인한다.
        // 응답 상태가 400 Bad Request인지 확인한다.
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
        // 수정 요청에 대한 서비스 반환값을 준비한다.
        when(activityCommandService.updateActivity(eq(1L), any(), any())).thenReturn(buildActivity());

        // 유효한 수정 요청이 200 응답으로 처리되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(put("/api/activities/1")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "activity_date": "2025-04-01",
                                    "activity_type": "issue",
                                    "activity_title": "긴급 이슈",
                                    "activity_content": null,
                                    "po_id": null,
                                    "activity_priority": null,
                                    "activity_schedule_from": null,
                                    "activity_schedule_to": null
                                }
                                """))
                .andExpect(status().isOk())
                // 응답 본문에 activity_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_id").exists());
    }

    @Test
    @DisplayName("PUT /api/activities/{activity_id} - 존재하지 않는 ID → 404 Not Found")
    void updateActivity_returns404WhenNotFound() throws Exception {
        // 서비스 조회 실패 예외가 404로 변환되는지 검증한다.
        when(activityCommandService.updateActivity(eq(999L), any(), any()))
                .thenThrow(new IllegalArgumentException("활동을 찾을 수 없습니다."));

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(put("/api/activities/999")
                        .with(csrf())
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "activity_date": "2025-04-01",
                                    "activity_type": "issue",
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
        // 삭제 요청이 예외 없이 끝나도록 서비스 목 객체를 설정한다.
        doNothing().when(activityCommandService).deleteActivity(1L);

        // 정상 삭제 요청이 204 응답으로 처리되는지 확인한다.
        // 응답 상태가 204 No Content인지 확인한다.
        mockMvc.perform(delete("/api/activities/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/activities/{activity_id} - 존재하지 않는 ID → 404 Not Found")
    void deleteActivity_returns404WhenNotFound() throws Exception {
        // 삭제 대상이 없을 때 404 응답으로 변환되는지 검증한다.
        org.mockito.Mockito.doThrow(new IllegalArgumentException("활동을 찾을 수 없습니다."))
                .when(activityCommandService).deleteActivity(999L);

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(delete("/api/activities/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
