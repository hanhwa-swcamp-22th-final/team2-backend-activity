package com.team2.activity.integration;

import com.team2.activity.command.domain.repository.ActivityPackageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

// ActivityPackage API의 생성, 조회, 수정, 삭제가 전체 계층에서 동작하는지 검증한다.
@DisplayName("ActivityPackage 통합 테스트")
class ActivityPackageIntegrationTest extends IntegrationTestSupport {

    // 삭제 이후 DB 상태를 직접 확인할 repository다.
    @Autowired
    private ActivityPackageRepository activityPackageRepository;

    @Test
    @DisplayName("패키지 생성 후 조회, 수정, 삭제까지 통합 흐름을 검증한다")
    void activityPackageCrudFlow() throws Exception {
        // 패키지 생성 요청을 보내고 생성된 package_id를 응답에서 추출한다.
        MvcResult createResult = mockMvc.perform(post("/api/activity-packages")
                        .with(csrf())
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "package_title": "주간 패키지",
                                  "package_description": "주간 활동 묶음",
                                  "po_id": "PO-001",
                                  "activity_ids": [100, 101],
                                  "viewer_ids": [2, 3]
                                }
                                """))
                .andExpect(status().isCreated())
                // 생성 응답에 package_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.package_id").exists())
                // 생성 응답에 creator_id가 헤더 값으로 반영됐는지 확인한다.
                .andExpect(jsonPath("$.creator_id").value(7))
                // 생성 응답의 첫 activity_id가 요청 값과 같은지 확인한다.
                .andExpect(jsonPath("$.activity_ids[0]").value(100))
                // 생성 응답의 첫 viewer_id가 요청 값과 같은지 확인한다.
                .andExpect(jsonPath("$.viewer_ids[0]").value(2))
                .andReturn();

        // 후속 요청에 사용할 package_id를 응답 본문에서 읽어 온다.
        long packageId = extractLong(createResult, "package_id");

        // 생성된 패키지가 상세 조회 API에서 조회되는지 확인한다.
        mockMvc.perform(get("/api/activity-packages/{packageId}", packageId))
                .andExpect(status().isOk())
                // 상세 응답의 package_id가 생성한 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.package_id").value(packageId))
                // 상세 응답의 제목이 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.package_title").value("주간 패키지"))
                // 상세 응답의 두 번째 activity_id가 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.activity_ids[1]").value(101))
                // 상세 응답의 두 번째 viewer_id가 생성 값과 같은지 확인한다.
                .andExpect(jsonPath("$.viewer_ids[1]").value(3));

        // 수정 요청을 통해 본문과 컬렉션 필드가 모두 갱신되는지 확인한다.
        mockMvc.perform(put("/api/activity-packages/{packageId}", packageId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "package_title": "월간 패키지",
                                  "package_description": "월간 활동 묶음",
                                  "po_id": "PO-2025-002",
                                  "activity_ids": [200, 300],
                                  "viewer_ids": [30, 40]
                                }
                                """))
                .andExpect(status().isOk())
                // 수정 응답의 package_id가 기존 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.package_id").value(packageId))
                // 수정 응답의 제목이 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.package_title").value("월간 패키지"))
                // 수정 응답의 PO ID가 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.po_id").value("PO-2025-002"))
                // 수정 응답의 첫 activity_id가 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.activity_ids[0]").value(200))
                // 수정 응답의 첫 viewer_id가 새 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.viewer_ids[0]").value(30));

        // 수정된 패키지 상태를 DB에 즉시 반영한다.
        activityPackageRepository.flush();

        // 목록 조회에서 수정된 제목이 노출되는지 확인한다.
        mockMvc.perform(get("/api/activity-packages").param("creatorId", "7"))
                .andExpect(status().isOk())
                // 목록 응답 첫 원소의 package_id가 수정한 패키지 ID와 같은지 확인한다.
                .andExpect(jsonPath("$.content[0].package_id").value(packageId))
                // 목록 응답 첫 원소의 제목이 수정 값으로 바뀌었는지 확인한다.
                .andExpect(jsonPath("$.content[0].package_title").value("월간 패키지"));

        // 삭제 요청이 204 응답으로 처리되는지 확인한다.
        mockMvc.perform(delete("/api/activity-packages/{packageId}", packageId).with(csrf()))
                .andExpect(status().isNoContent());

        // 삭제 결과를 DB에 반영한다.
        activityPackageRepository.flush();

        // 최종적으로 패키지가 DB에서 제거됐는지 확인한다.
        assertThat(activityPackageRepository.findById(packageId)).isEmpty();
    }

    @Test
    @DisplayName("필수 필드 없이 패키지 생성 시 400을 반환한다")
    void createPackage_returns400WhenTitleMissing() throws Exception {
        // package_title은 @NotBlank 필수 필드이므로 누락 시 400이어야 한다.
        mockMvc.perform(post("/api/activity-packages")
                        .with(csrf())
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        // package_title을 포함하지 않는 요청을 전송한다.
                        .content("""
                                {
                                  "package_description": "설명만 있음",
                                  "activity_ids": [1],
                                  "viewer_ids": [2]
                                }
                                """))
                // 유효성 검증 실패로 400 Bad Request가 반환되는지 확인한다.
                .andExpect(status().isBadRequest())
                // 응답 본문에 message 필드가 포함되는지 확인한다.
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("존재하지 않는 패키지 단건 조회 시 404를 반환한다")
    void getPackage_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 단건 조회 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(get("/api/activity-packages/{packageId}", 99999L))
                // 패키지 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("활동 패키지를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 패키지 수정 시 404를 반환한다")
    void updatePackage_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 수정 시도 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(put("/api/activity-packages/{packageId}", 99999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        // 유효한 수정 요청 본문을 전송해 존재 여부에서 실패하도록 한다.
                        .content("""
                                {
                                  "package_title": "없는 패키지 수정"
                                }
                                """))
                // 패키지 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("활동 패키지를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 패키지 삭제 시 404를 반환한다")
    void deletePackage_returns404WhenNotFound() throws Exception {
        // 존재하지 않는 ID로 삭제 시도 시 IllegalArgumentException → 404여야 한다.
        mockMvc.perform(delete("/api/activity-packages/{packageId}", 99999L).with(csrf()))
                // 패키지 없음 예외가 404로 변환되는지 확인한다.
                .andExpect(status().isNotFound())
                // 응답 본문의 메시지가 정확한지 확인한다.
                .andExpect(jsonPath("$.message").value("활동 패키지를 찾을 수 없습니다."));
    }
}
