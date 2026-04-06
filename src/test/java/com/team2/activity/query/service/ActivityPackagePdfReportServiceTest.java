package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.PurchaseOrderResponse;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.common.PdfGenerationException;
import com.team2.activity.query.dto.ActivityResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// Spring 컨텍스트 없이 Mockito만으로 단위 테스트를 수행한다.
@ExtendWith(MockitoExtension.class)
// 이 클래스가 어떤 대상을 테스트하는지 IDE와 리포트에 표시되는 이름이다.
@DisplayName("ActivityPackagePdfReportService 테스트")
class ActivityPackagePdfReportServiceTest {

    // 활동 단건 조회 의존성을 가짜 객체로 대체한다.
    @Mock
    private ActivityQueryService activityQueryService;

    // 사용자 이름 조회(auth 서비스) 의존성을 가짜 객체로 대체한다.
    @Mock
    private AuthFeignClient authFeignClient;

    // PO 정보 조회(document 서비스) 의존성을 가짜 객체로 대체한다.
    @Mock
    private DocumentsFeignClient documentsFeignClient;

    // @Mock 필드들을 자동으로 주입해 실제 테스트 대상 인스턴스를 생성한다.
    @InjectMocks
    private ActivityPackagePdfReportService pdfReportService;

    // 각 테스트에서 반복되는 ActivityResponse 생성을 공통 헬퍼로 추출했다.
    private ActivityResponse buildActivity(Long id, ActivityType type, String title) {
        // id와 type, title만 다르고 나머지 필드는 고정값으로 채운 활동 응답 객체를 만든다.
        return new ActivityResponse(id, 10L, "PO-001", 1L, LocalDate.of(2026, 1, 15),
                type, title, "내용", null,
                // 일정(SCHEDULE) 타입일 때만 시작일과 종료일을 채우고 나머지는 null로 둔다.
                type == ActivityType.SCHEDULE ? LocalDate.of(2026, 1, 20) : null,
                type == ActivityType.SCHEDULE ? LocalDate.of(2026, 1, 25) : null,
                null, null, "작성자", "거래처");
    }

    // ───────────────────────────────────────────────
    // 성공 케이스
    // ───────────────────────────────────────────────
    // 정상적으로 PDF가 생성되는 시나리오를 묶는 중첩 클래스다.
    @Nested
    @DisplayName("성공 케이스")
    class SuccessTests {

        @Test
        @DisplayName("여러 유형의 활동이 포함된 패키지 PDF를 정상 생성한다")
        void generatePackageReport_success_withMultipleActivityTypes() {
            // 실제 ActivityPackage 객체 대신 행위를 제어할 수 있는 mock을 만든다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 패키지 제목이 호출될 때 반환할 값을 지정한다.
            when(pkg.getPackageTitle()).thenReturn("2026 Q1 영업활동 보고서");

            // 패키지에 포함된 첫 번째 활동 항목 mock을 만든다.
            ActivityPackageItem item1 = mock(ActivityPackageItem.class);
            // 첫 번째 항목의 활동 ID를 1로 지정한다.
            when(item1.getActivityId()).thenReturn(1L);
            // 패키지에 포함된 두 번째 활동 항목 mock을 만든다.
            ActivityPackageItem item2 = mock(ActivityPackageItem.class);
            // 두 번째 항목의 활동 ID를 2로 지정한다.
            when(item2.getActivityId()).thenReturn(2L);
            // 패키지에 포함된 세 번째 활동 항목 mock을 만든다.
            ActivityPackageItem item3 = mock(ActivityPackageItem.class);
            // 세 번째 항목의 활동 ID를 3으로 지정한다.
            when(item3.getActivityId()).thenReturn(3L);
            // 패키지의 items() 호출 시 위 3개 항목을 반환하도록 설정한다.
            when(pkg.getItems()).thenReturn(List.of(item1, item2, item3));

            // ID 1번 활동은 MEETING 유형으로 조회되도록 설정한다.
            when(activityQueryService.getActivity(1L)).thenReturn(buildActivity(1L, ActivityType.MEETING, "미팅 활동"));
            // ID 2번 활동은 ISSUE 유형으로 조회되도록 설정한다.
            when(activityQueryService.getActivity(2L)).thenReturn(buildActivity(2L, ActivityType.ISSUE, "이슈 활동"));
            // ID 3번 활동은 SCHEDULE 유형으로 조회되도록 설정한다(시작일·종료일 포함).
            when(activityQueryService.getActivity(3L)).thenReturn(buildActivity(3L, ActivityType.SCHEDULE, "일정 활동"));

            // auth 서비스에서 반환할 사용자 정보를 mock으로 만든다.
            UserResponse user = mock(UserResponse.class);
            // 사용자 이름을 "김영업"으로 지정한다.
            when(user.getName()).thenReturn("김영업");
            // userId 7로 auth 서비스를 호출하면 위 사용자 응답을 반환하도록 설정한다.
            when(authFeignClient.getUser(7L)).thenReturn(user);

            // 실제 PDF 생성 메서드를 호출한다. userId 7을 작성자로 전달한다.
            byte[] pdf = pdfReportService.generatePackageReport(pkg, 7L);

            // 생성된 PDF 바이트 배열이 비어 있지 않아야 한다.
            assertThat(pdf).isNotEmpty();
            // PDF 파일 시그니처는 반드시 '%PDF'로 시작해야 한다.
            assertThat(new String(pdf, 0, 5)).startsWith("%PDF");
            // 다운로드 파일명이 패키지 제목 그대로 반환되는지 확인한다.
            assertThat(pdfReportService.getDownloadFileName(pkg)).isEqualTo("2026 Q1 영업활동 보고서.pdf");
        }
    }

    // ───────────────────────────────────────────────
    // 실패 / 엣지 케이스
    // ───────────────────────────────────────────────
    // 예외 상황이나 경계값에서 서비스가 올바르게 동작하는지 검증하는 중첩 클래스다.
    @Nested
    @DisplayName("실패 및 엣지 케이스")
    class FailureTests {

        @Test
        @DisplayName("패키지에 활동이 없으면 빈 테이블로 PDF를 생성한다")
        void generatePackageReport_emptyActivities() {
            // ActivityPackage mock을 생성한다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 패키지 제목을 "빈 패키지"로 지정한다.
            when(pkg.getPackageTitle()).thenReturn("빈 패키지");
            // 패키지에 포함된 활동 목록을 빈 리스트로 설정한다.
            when(pkg.getItems()).thenReturn(List.of());

            // auth 서비스에서 반환할 사용자 mock을 만든다.
            UserResponse user = mock(UserResponse.class);
            // 사용자 이름을 "작성자"로 지정한다.
            when(user.getName()).thenReturn("작성자");
            // userId 1로 auth 서비스를 호출하면 위 사용자 응답을 반환하도록 설정한다.
            when(authFeignClient.getUser(1L)).thenReturn(user);

            // 활동이 없는 패키지로 PDF 생성을 요청한다.
            byte[] pdf = pdfReportService.generatePackageReport(pkg, 1L);

            // 활동이 없어도 PDF 자체는 정상 생성돼야 한다.
            assertThat(pdf).isNotEmpty();
            // 활동이 없으므로 activityQueryService.getActivity()는 한 번도 호출되지 않아야 한다.
            verify(activityQueryService, never()).getActivity(anyLong());
        }

        @Test
        @DisplayName("삭제된 활동은 건너뛰고 나머지로 PDF를 생성한다")
        void generatePackageReport_skipsDeletedActivity() {
            // ActivityPackage mock을 생성한다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 패키지 제목을 지정한다.
            when(pkg.getPackageTitle()).thenReturn("삭제된 활동 포함");

            // 정상 활동 항목 mock을 만든다.
            ActivityPackageItem item1 = mock(ActivityPackageItem.class);
            // 정상 활동의 ID를 1로 지정한다.
            when(item1.getActivityId()).thenReturn(1L);
            // 삭제된(존재하지 않는) 활동 항목 mock을 만든다.
            ActivityPackageItem item2 = mock(ActivityPackageItem.class);
            // 존재하지 않는 활동의 ID를 999로 지정한다.
            when(item2.getActivityId()).thenReturn(999L);
            // 패키지가 정상 활동 1개와 삭제된 활동 1개를 포함하도록 설정한다.
            when(pkg.getItems()).thenReturn(List.of(item1, item2));

            // ID 1번 활동은 정상 조회되도록 설정한다.
            when(activityQueryService.getActivity(1L)).thenReturn(buildActivity(1L, ActivityType.MEMO, "정상 메모"));
            // ID 999번 활동은 DB에 없으므로 예외를 던지도록 설정한다.
            when(activityQueryService.getActivity(999L)).thenThrow(new IllegalArgumentException("활동을 찾을 수 없습니다."));

            // auth 서비스에서 반환할 사용자 mock을 만든다.
            UserResponse user = mock(UserResponse.class);
            // 사용자 이름을 "작성자"로 지정한다.
            when(user.getName()).thenReturn("작성자");
            // userId 1로 auth 서비스를 호출하면 위 사용자 응답을 반환하도록 설정한다.
            when(authFeignClient.getUser(1L)).thenReturn(user);

            // 삭제된 활동이 포함된 패키지로 PDF 생성을 요청한다.
            byte[] pdf = pdfReportService.generatePackageReport(pkg, 1L);

            // 삭제된 활동은 건너뛰고 나머지 활동으로 PDF가 생성돼야 한다.
            assertThat(pdf).isNotEmpty();
        }

        @Test
        @DisplayName("작성자 이름 조회 실패 시 ID 문자열로 대체한다")
        void generatePackageReport_authorFeignFail_fallbackToId() {
            // ActivityPackage mock을 생성한다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 패키지 제목을 지정한다.
            when(pkg.getPackageTitle()).thenReturn("Feign 실패 테스트");
            // 패키지에 활동이 없도록 빈 리스트를 반환한다.
            when(pkg.getItems()).thenReturn(List.of());

            // auth 서비스 호출 시 네트워크 장애 상황을 시뮬레이션한다.
            when(authFeignClient.getUser(5L)).thenThrow(new RuntimeException("Auth 서비스 장애"));

            // auth 서비스가 실패해도 PDF 생성이 완료돼야 한다.
            byte[] pdf = pdfReportService.generatePackageReport(pkg, 5L);

            // auth 서비스 장애 시 예외를 전파하지 않고 PDF를 정상 생성하는지 확인한다.
            assertThat(pdf).isNotEmpty();
        }

        @Test
        @DisplayName("userId와 creatorId 모두 null이면 작성자 '-'로 표시한다")
        void generatePackageReport_noUserIdNoCreatorId() {
            // ActivityPackage mock을 생성한다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 패키지 제목을 지정한다.
            when(pkg.getPackageTitle()).thenReturn("작성자 없음");
            // 패키지에 저장된 creatorId를 null로 설정한다.
            when(pkg.getCreatorId()).thenReturn(null);
            // 패키지에 활동이 없도록 빈 리스트를 반환한다.
            when(pkg.getItems()).thenReturn(List.of());

            // 헤더 userId도 null로 전달해 작성자 정보가 아예 없는 상황을 만든다.
            byte[] pdf = pdfReportService.generatePackageReport(pkg, null);

            // userId와 creatorId 모두 없어도 PDF가 정상 생성돼야 한다.
            assertThat(pdf).isNotEmpty();
        }

        @Test
        @DisplayName("패키지 제목과 PO ID 모두 없으면 '- PKG' 기본 제목을 사용한다")
        void getDownloadFileName_noTitleNoPoId() {
            // ActivityPackage mock을 생성한다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 패키지 제목을 빈 문자열로 설정해 제목이 없는 상황을 시뮬레이션한다.
            when(pkg.getPackageTitle()).thenReturn("");
            // PO ID도 null로 설정해 파일명 생성에 사용할 정보가 아무것도 없도록 한다.
            when(pkg.getPoId()).thenReturn(null);

            // 파일명 생성 메서드를 호출한다.
            String fileName = pdfReportService.getDownloadFileName(pkg);

            // 제목과 PO ID 모두 없을 때 기본 파일명 '- PKG.pdf'가 반환되는지 확인한다.
            assertThat(fileName).isEqualTo("- PKG.pdf");
        }

        @Test
        @DisplayName("PO 번호 조회 실패 시 원본 poId로 파일명을 생성한다")
        void getDownloadFileName_poFeignFail_fallbackToPoId() {
            // ActivityPackage mock을 생성한다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 패키지 제목을 빈 문자열로 설정해 PO ID 기반 파일명 생성 경로를 유도한다.
            when(pkg.getPackageTitle()).thenReturn("");
            // PO ID를 "PO-500"으로 설정한다.
            when(pkg.getPoId()).thenReturn("PO-500");
            // document 서비스 호출 시 장애 상황을 시뮬레이션한다.
            when(documentsFeignClient.getPurchaseOrder("PO-500")).thenThrow(new RuntimeException("Documents 서비스 장애"));

            // 파일명 생성 메서드를 호출한다.
            String fileName = pdfReportService.getDownloadFileName(pkg);

            // document 서비스 실패 시 원본 poId를 그대로 파일명에 사용하는지 확인한다.
            assertThat(fileName).isEqualTo("PO-500 PKG.pdf");
        }

        @Test
        @DisplayName("파일명에 특수문자가 포함되면 밑줄로 치환한다")
        void getDownloadFileName_sanitizesSpecialCharacters() {
            // ActivityPackage mock을 생성한다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 파일 시스템에서 사용할 수 없는 특수문자가 포함된 제목을 설정한다.
            when(pkg.getPackageTitle()).thenReturn("위험한/파일명:*?\"<>|");

            // 특수문자가 포함된 제목으로 파일명 생성을 요청한다.
            String fileName = pdfReportService.getDownloadFileName(pkg);

            // 특수문자가 밑줄로 치환된 파일명이 반환되는지 확인한다.
            assertThat(fileName).isEqualTo("위험한_파일명_.pdf");
        }

        @Test
        @DisplayName("패키지 제목이 특수문자로만 구성되면 치환된 문자로 파일명을 생성한다")
        void getDownloadFileName_allSpecialChars() {
            // ActivityPackage mock을 생성한다.
            ActivityPackage pkg = mock(ActivityPackage.class);
            // 제목 전체가 특수문자로만 이뤄진 극단적 케이스를 설정한다.
            when(pkg.getPackageTitle()).thenReturn("/:*?\"<>|");

            // 전체 특수문자 제목으로 파일명 생성을 요청한다.
            String fileName = pdfReportService.getDownloadFileName(pkg);

            // 치환 후에도 .pdf 확장자가 붙어야 한다.
            assertThat(fileName).endsWith(".pdf");
            // 파일명에 특수문자가 하나도 남아 있지 않아야 한다.
            assertThat(fileName).doesNotContain("/", ":", "*", "?", "\"", "<", ">", "|");
        }
    }
}
