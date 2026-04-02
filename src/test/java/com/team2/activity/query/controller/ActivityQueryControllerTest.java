package com.team2.activity.query.controller;

import com.team2.activity.command.domain.entity.Activity;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.query.dto.ActivityResponse;
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

// Activity 조회 API가 서비스 결과를 올바른 HTTP 응답으로 바꾸는지 검증한다.
@WebMvcTest(ActivityQueryController.class)
@WithMockUser
@DisplayName("ActivityQueryController 테스트")
class ActivityQueryControllerTest {

    // HTTP 요청/응답을 흉내 내는 MockMvc다.
    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 호출할 조회 서비스 목 객체다.
    @MockBean
    private ActivityQueryService activityQueryService;

    // 응답 직렬화 검증에 사용할 공통 Activity 픽스처를 만든다.
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
                // 테스트용 활동 본문을 설정한다.
                .activityContent("테스트 내용")
                // 테스트용 활동 날짜를 설정한다.
                .activityDate(LocalDate.of(2025, 4, 1))
                // 공통 Activity 픽스처 생성을 마무리한다.
                .build();
    }

    // 상세 조회 응답 검증에 사용할 ActivityResponse 픽스처를 만든다.
    private ActivityResponse buildActivityResponse() {
        // 공통 Activity 엔티티 픽스처를 DTO로 변환해 반환한다.
        return ActivityResponse.from(buildActivity(), "홍길동", "고객사");
    }

    @Test
    @DisplayName("GET /api/activities → 200 OK, 페이징 응답 구조 포함")
    void getActivities_returns200WithPagedResult() throws Exception {
        // 전체 조회 서비스 결과를 목으로 준비한다.
        when(activityQueryService.getAllActivities()).thenReturn(List.of(buildActivity()));

        // 목록 조회 응답이 페이징 래퍼 구조를 갖는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/activities"))
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
    @DisplayName("GET /api/activities?client_id=1 → 200 OK, client_id 필터 적용")
    void getActivities_returns200WithClientIdFilter() throws Exception {
        // client_id 필터가 적용된 결과를 서비스가 반환하도록 설정한다.
        when(activityQueryService.getActivitiesByClientId(1L)).thenReturn(List.of(buildActivity()));

        // client_id 파라미터 요청이 정상 응답으로 변환되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/activities").param("clientId", "1"))
                .andExpect(status().isOk())
                // content 필드가 배열인지 확인한다.
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/activities/{activity_id} → 200 OK, 상세 필드 포함")
    void getActivity_returns200WithDetail() throws Exception {
        // 단건 조회 결과를 서비스 목 객체에 등록한다.
        when(activityQueryService.getActivity(1L)).thenReturn(buildActivityResponse());

        // 상세 조회 응답에 핵심 필드가 포함되는지 확인한다.
        // 응답 상태가 200 OK인지 확인한다.
        mockMvc.perform(get("/api/activities/1"))
                .andExpect(status().isOk())
                // 상세 응답에 activity_id가 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_id").exists())
                // 상세 응답에 activity_type이 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_type").exists())
                // 상세 응답에 activity_title이 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_title").exists())
                // 상세 응답에 activity_content가 포함되는지 확인한다.
                .andExpect(jsonPath("$.activity_content").exists());
    }

    @Test
    @DisplayName("GET /api/activities/{activity_id} - 존재하지 않는 ID → 404 Not Found")
    void getActivity_returns404WhenNotFound() throws Exception {
        // 서비스 예외가 404 응답으로 변환되는지 검증한다.
        when(activityQueryService.getActivity(999L))
                .thenThrow(new IllegalArgumentException("활동을 찾을 수 없습니다."));

        // 응답 상태가 404 Not Found인지 확인한다.
        mockMvc.perform(get("/api/activities/999"))
                .andExpect(status().isNotFound());
    }
}
