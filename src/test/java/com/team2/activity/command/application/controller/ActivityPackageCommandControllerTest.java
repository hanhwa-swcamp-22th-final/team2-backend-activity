package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ActivityPackageCommandService;
import com.team2.activity.command.domain.entity.ActivityPackage;
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

// ActivityPackage 쓰기 API의 생성, 수정, 삭제 응답을 검증한다.
@WebMvcTest(ActivityPackageCommandController.class)
@WithMockUser
@DisplayName("ActivityPackageCommandController 테스트")
class ActivityPackageCommandControllerTest {

    // 컨트롤러 요청을 실행하는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 의존하는 패키지 command 서비스 목 객체다.
    @MockBean
    private ActivityPackageCommandService activityPackageCommandService;

    // 응답 검증에 사용할 공통 ActivityPackage 픽스처다.
    private ActivityPackage buildPackage() {
        return ActivityPackage.builder()
                // 테스트용 패키지 ID를 설정한다.
                .packageId(1L)
                // 테스트용 패키지 제목을 설정한다.
                .packageTitle("주간 패키지")
                // 테스트용 생성자 ID를 설정한다.
                .creatorId(10L)
                // 공통 ActivityPackage 픽스처 생성을 마무리한다.
                .build();
    }

    @Test
    @DisplayName("POST /api/activity-packages → 201 Created, package_id 포함")
    void createPackage_returns201() throws Exception {
        // 생성 요청에 대한 서비스 반환값을 준비한다.
        when(activityPackageCommandService.createPackage(any())).thenReturn(buildPackage());

        // 유효한 생성 요청이 201 응답으로 처리되는지 확인한다.
        // 응답 상태가 201 Created인지 확인한다.
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
                // 응답 본문에 package_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.package_id").exists());

        // 헤더의 사용자 ID가 creatorId로 매핑됐는지 검증한다.
        verify(activityPackageCommandService).createPackage(argThat(p -> Long.valueOf(10L).equals(p.getCreatorId())));
    }

    @Test
    @DisplayName("POST /api/activity-packages - 필수 필드 누락 → 400 Bad Request")
    void createPackage_returns400WhenRequiredFieldMissing() throws Exception {
        // 필수 필드가 없는 요청이 검증 오류로 처리되는지 확인한다.
        // 응답 상태가 400 Bad Request인지 확인한다.
        mockMvc.perform(post("/api/activity-packages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/activity-packages/{package_id} → 200 OK")
    void updatePackage_returns200() throws Exception {
        // 수정 요청에 대한 서비스 반환값을 준비한다.
        when(activityPackageCommandService.updateAll(eq(1L), any())).thenReturn(buildPackage());

        // 유효한 수정 요청이 200 응답으로 처리되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
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
                // 응답 본문에 package_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.package_id").exists());
    }

    @Test
    @DisplayName("PUT /api/activity-packages/{package_id} - 존재하지 않는 ID → 404 Not Found")
    void updatePackage_returns404WhenNotFound() throws Exception {
        // 서비스 조회 실패 예외가 404로 바뀌는지 검증한다.
        when(activityPackageCommandService.updateAll(eq(999L), any()))
                .thenThrow(new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."));

        // 응답 상태가 404 Not Found인지 확인한다.
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
        // 삭제 요청이 성공하도록 서비스 목 객체를 준비한다.
        doNothing().when(activityPackageCommandService).deletePackage(1L);

        // 정상 삭제 요청이 204 응답으로 처리되는지 확인한다.
        // 응답 상태가 204 No Content인지 확인한다.
        mockMvc.perform(delete("/api/activity-packages/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/activity-packages/{package_id} - 존재하지 않는 ID → 404 Not Found")
    void deletePackage_returns404WhenNotFound() throws Exception {
        // 삭제 대상이 없을 때 404 응답으로 변환되는지 검증한다.
        org.mockito.Mockito.doThrow(new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."))
                .when(activityPackageCommandService).deletePackage(999L);

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(delete("/api/activity-packages/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
