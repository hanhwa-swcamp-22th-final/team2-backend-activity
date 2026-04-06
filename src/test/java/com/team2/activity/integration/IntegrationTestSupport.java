package com.team2.activity.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.MasterFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

// 통합 테스트가 공통으로 사용하는 설정과 유틸리티를 제공하는 추상 기반 클래스다.
@SpringBootTest
// MockMvc 자동 구성을 활성화해 컨트롤러 계층을 HTTP 요청으로 테스트할 수 있게 한다.
@AutoConfigureMockMvc
// 각 테스트 메서드 종료 시 DB 변경사항을 롤백해 테스트 독립성을 보장한다.
@Transactional
// 인증이 필요한 엔드포인트 테스트에서 사용할 목 사용자를 적용한다.
@WithMockUser
// test 프로파일을 활성화해 H2 인메모리 DB 환경을 사용하도록 한다.
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    // 외부 문서 서비스 호출 없이 발주 정보 조회를 시뮬레이션할 목 빈을 등록한다.
    @MockBean
    protected DocumentsFeignClient documentsFeignClient;

    // 외부 인증 서비스 호출 없이 사용자 조회를 시뮬레이션할 목 빈을 등록한다.
    @MockBean
    protected AuthFeignClient authFeignClient;

    // 외부 마스터 서비스 호출 없이 거래처 조회를 시뮬레이션할 목 빈을 등록한다.
    @MockBean
    protected MasterFeignClient masterFeignClient;

    // 실제 HTTP 요청처럼 API를 호출하는 MockMvc를 자동 주입한다.
    @Autowired
    protected MockMvc mockMvc;

    // 응답 JSON에서 특정 필드를 추출할 때 사용하는 ObjectMapper를 자동 주입한다.
    @Autowired
    protected ObjectMapper objectMapper;

    // JSON 응답 본문에서 지정한 이름의 숫자 필드를 long으로 추출한다.
    protected long extractLong(MvcResult result, String fieldName) throws Exception {
        // 응답 본문 문자열 전체를 JSON 트리 구조로 파싱한다.
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        // 지정한 필드 이름으로 노드를 찾아 long 값으로 변환해 반환한다.
        return jsonNode.get(fieldName).asLong();
    }
}
