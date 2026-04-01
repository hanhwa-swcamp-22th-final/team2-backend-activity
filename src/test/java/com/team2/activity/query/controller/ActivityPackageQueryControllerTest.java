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

// ActivityPackage 조회 API의 응답 구조와 예외 변환을 검증한다.
@WebMvcTest(ActivityPackageQueryController.class)
@WithMockUser
@DisplayName("ActivityPackageQueryController 테스트")
class ActivityPackageQueryControllerTest {

    // 컨트롤러 HTTP 요청을 수행하는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 호출할 패키지 조회 서비스 목 객체다.
    @MockBean
    private ActivityPackageQueryService activityPackageQueryService;

    // 응답 검증에 사용할 공통 ActivityPackage 픽스처를 만든다.
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
    @DisplayName("GET /api/activity-packages → 200 OK, 페이징 응답 구조 포함")
    void getPackages_returns200WithPagedResult() throws Exception {
        // 전체 패키지 목록 응답을 서비스 목 객체에 등록한다.
        when(activityPackageQueryService.getAllPackages()).thenReturn(List.of(buildPackage()));

        // 목록 조회 응답이 페이징 구조를 유지하는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/activity-packages"))
                .andExpect(status().isOk())
                // content 필드가 배열인지 확인한다.
                .andExpect(jsonPath("$.content").isArray())
                // total_elements 필드가 존재하는지 확인한다.
                .andExpect(jsonPath("$.total_elements").exists())
                // total_pages 필드가 존재하는지 확인한다.
                .andExpect(jsonPath("$.total_pages").exists())
                // current_page 필드가 존재하는지 확인한다.
                .andExpect(jsonPath("$.current_page").exists());
    }

    @Test
    @DisplayName("GET /api/activity-packages?creator_id=10 → 200 OK, creator_id 필터 적용")
    void getPackages_returns200WithCreatorIdFilter() throws Exception {
        // creator_id 조건 조회 결과를 서비스가 반환하도록 설정한다.
        when(activityPackageQueryService.getPackagesByCreatorId(10L)).thenReturn(List.of(buildPackage()));

        // 생성자 필터 요청이 정상 응답으로 처리되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/activity-packages").param("creator_id", "10"))
                .andExpect(status().isOk())
                // content 필드가 배열인지 확인한다.
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/activity-packages/{package_id} → 200 OK, 상세 필드 포함")
    void getPackage_returns200WithDetail() throws Exception {
        // 단건 패키지 조회 결과를 준비한다.
        when(activityPackageQueryService.getPackage(1L)).thenReturn(buildPackage());

        // 상세 응답에 컬렉션 필드까지 포함되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/activity-packages/1"))
                .andExpect(status().isOk())
                // 상세 응답에 package_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.package_id").exists())
                // 상세 응답에 package_title이 포함되는지 확인한다.
                .andExpect(jsonPath("$.package_title").exists())
                // 상세 응답에 activity_ids 배열이 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_ids").isArray())
                // 상세 응답에 viewer_ids 배열이 포함되는지 확인한다.
                .andExpect(jsonPath("$.viewer_ids").isArray());
    }

    @Test
    @DisplayName("GET /api/activity-packages/{package_id} - 존재하지 않는 ID → 404 Not Found")
    void getPackage_returns404WhenNotFound() throws Exception {
        // 서비스 조회 실패 예외가 404로 매핑되는지 검증한다.
        when(activityPackageQueryService.getPackage(999L))
                .thenThrow(new IllegalArgumentException("활동 패키지를 찾을 수 없습니다."));

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(get("/api/activity-packages/999"))
                .andExpect(status().isNotFound());
    }
}
