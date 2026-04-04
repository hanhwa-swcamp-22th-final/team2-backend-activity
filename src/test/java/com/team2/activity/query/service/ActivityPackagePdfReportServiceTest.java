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

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityPackagePdfReportService 테스트")
class ActivityPackagePdfReportServiceTest {

    @Mock
    private ActivityQueryService activityQueryService;

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private DocumentsFeignClient documentsFeignClient;

    @InjectMocks
    private ActivityPackagePdfReportService pdfReportService;

    private ActivityResponse buildActivity(Long id, ActivityType type, String title) {
        return new ActivityResponse(id, 10L, "PO-001", 1L, LocalDate.of(2026, 1, 15),
                type, title, "내용", null,
                type == ActivityType.SCHEDULE ? LocalDate.of(2026, 1, 20) : null,
                type == ActivityType.SCHEDULE ? LocalDate.of(2026, 1, 25) : null,
                null, null, "작성자", "거래처");
    }

    // ───────────────────────────────────────────────
    // 성공 케이스
    // ───────────────────────────────────────────────
    @Nested
    @DisplayName("성공 케이스")
    class SuccessTests {

        @Test
        @DisplayName("여러 유형의 활동이 포함된 패키지 PDF를 정상 생성한다")
        void generatePackageReport_success_withMultipleActivityTypes() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("2026 Q1 영업활동 보고서");

            ActivityPackageItem item1 = mock(ActivityPackageItem.class);
            when(item1.getActivityId()).thenReturn(1L);
            ActivityPackageItem item2 = mock(ActivityPackageItem.class);
            when(item2.getActivityId()).thenReturn(2L);
            ActivityPackageItem item3 = mock(ActivityPackageItem.class);
            when(item3.getActivityId()).thenReturn(3L);
            when(pkg.getItems()).thenReturn(List.of(item1, item2, item3));

            when(activityQueryService.getActivity(1L)).thenReturn(buildActivity(1L, ActivityType.MEETING, "미팅 활동"));
            when(activityQueryService.getActivity(2L)).thenReturn(buildActivity(2L, ActivityType.ISSUE, "이슈 활동"));
            when(activityQueryService.getActivity(3L)).thenReturn(buildActivity(3L, ActivityType.SCHEDULE, "일정 활동"));

            UserResponse user = mock(UserResponse.class);
            when(user.getName()).thenReturn("김영업");
            when(authFeignClient.getUser(7L)).thenReturn(user);

            byte[] pdf = pdfReportService.generatePackageReport(pkg, 7L);

            assertThat(pdf).isNotEmpty();
            assertThat(new String(pdf, 0, 5)).startsWith("%PDF");
            assertThat(pdfReportService.getDownloadFileName(pkg)).isEqualTo("2026 Q1 영업활동 보고서.pdf");
        }
    }

    // ───────────────────────────────────────────────
    // 실패 / 엣지 케이스
    // ───────────────────────────────────────────────
    @Nested
    @DisplayName("실패 및 엣지 케이스")
    class FailureTests {

        @Test
        @DisplayName("패키지에 활동이 없으면 빈 테이블로 PDF를 생성한다")
        void generatePackageReport_emptyActivities() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("빈 패키지");
            when(pkg.getItems()).thenReturn(List.of());

            UserResponse user = mock(UserResponse.class);
            when(user.getName()).thenReturn("작성자");
            when(authFeignClient.getUser(1L)).thenReturn(user);

            byte[] pdf = pdfReportService.generatePackageReport(pkg, 1L);

            assertThat(pdf).isNotEmpty();
            verify(activityQueryService, never()).getActivity(anyLong());
        }

        @Test
        @DisplayName("삭제된 활동은 건너뛰고 나머지로 PDF를 생성한다")
        void generatePackageReport_skipsDeletedActivity() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("삭제된 활동 포함");

            ActivityPackageItem item1 = mock(ActivityPackageItem.class);
            when(item1.getActivityId()).thenReturn(1L);
            ActivityPackageItem item2 = mock(ActivityPackageItem.class);
            when(item2.getActivityId()).thenReturn(999L);
            when(pkg.getItems()).thenReturn(List.of(item1, item2));

            when(activityQueryService.getActivity(1L)).thenReturn(buildActivity(1L, ActivityType.MEMO, "정상 메모"));
            when(activityQueryService.getActivity(999L)).thenThrow(new IllegalArgumentException("활동을 찾을 수 없습니다."));

            UserResponse user = mock(UserResponse.class);
            when(user.getName()).thenReturn("작성자");
            when(authFeignClient.getUser(1L)).thenReturn(user);

            byte[] pdf = pdfReportService.generatePackageReport(pkg, 1L);

            assertThat(pdf).isNotEmpty();
        }

        @Test
        @DisplayName("작성자 이름 조회 실패 시 ID 문자열로 대체한다")
        void generatePackageReport_authorFeignFail_fallbackToId() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("Feign 실패 테스트");
            when(pkg.getItems()).thenReturn(List.of());

            when(authFeignClient.getUser(5L)).thenThrow(new RuntimeException("Auth 서비스 장애"));

            byte[] pdf = pdfReportService.generatePackageReport(pkg, 5L);

            assertThat(pdf).isNotEmpty();
        }

        @Test
        @DisplayName("userId와 creatorId 모두 null이면 작성자 '-'로 표시한다")
        void generatePackageReport_noUserIdNoCreatorId() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("작성자 없음");
            when(pkg.getCreatorId()).thenReturn(null);
            when(pkg.getItems()).thenReturn(List.of());

            byte[] pdf = pdfReportService.generatePackageReport(pkg, null);

            assertThat(pdf).isNotEmpty();
        }

        @Test
        @DisplayName("패키지 제목과 PO ID 모두 없으면 '- PKG' 기본 제목을 사용한다")
        void getDownloadFileName_noTitleNoPoId() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("");
            when(pkg.getPoId()).thenReturn(null);

            String fileName = pdfReportService.getDownloadFileName(pkg);

            assertThat(fileName).isEqualTo("- PKG.pdf");
        }

        @Test
        @DisplayName("PO 번호 조회 실패 시 원본 poId로 파일명을 생성한다")
        void getDownloadFileName_poFeignFail_fallbackToPoId() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("");
            when(pkg.getPoId()).thenReturn("PO-500");
            when(documentsFeignClient.getPurchaseOrder("PO-500")).thenThrow(new RuntimeException("Documents 서비스 장애"));

            String fileName = pdfReportService.getDownloadFileName(pkg);

            assertThat(fileName).isEqualTo("PO-500 PKG.pdf");
        }

        @Test
        @DisplayName("파일명에 특수문자가 포함되면 밑줄로 치환한다")
        void getDownloadFileName_sanitizesSpecialCharacters() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("위험한/파일명:*?\"<>|");

            String fileName = pdfReportService.getDownloadFileName(pkg);

            assertThat(fileName).isEqualTo("위험한_파일명_.pdf");
        }

        @Test
        @DisplayName("패키지 제목이 특수문자로만 구성되면 치환된 문자로 파일명을 생성한다")
        void getDownloadFileName_allSpecialChars() {
            ActivityPackage pkg = mock(ActivityPackage.class);
            when(pkg.getPackageTitle()).thenReturn("/:*?\"<>|");

            String fileName = pdfReportService.getDownloadFileName(pkg);

            assertThat(fileName).endsWith(".pdf");
            assertThat(fileName).doesNotContain("/", ":", "*", "?", "\"", "<", ">", "|");
        }
    }
}
