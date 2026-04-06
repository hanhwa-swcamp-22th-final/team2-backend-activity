package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.domain.entity.enums.Priority;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.query.dto.ActivityResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Spring 컨텍스트 없이 Mockito만으로 PDF 파일을 실제 생성하는 샘플 생성기다.
@ExtendWith(MockitoExtension.class)
class PdfSampleGeneratorTest {

    @Mock
    private ActivityQueryService activityQueryService;

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private DocumentsFeignClient documentsFeignClient;

    @InjectMocks
    private ActivityPackagePdfReportService pdfReportService;

    @Test
    @DisplayName("샘플 PDF 파일 생성 — 프로젝트 루트/sample-report.pdf")
    void generateSamplePdf() throws Exception {
        // ── 패키지 mock 설정 ─────────────────────────────────────────
        ActivityPackage pkg = mock(ActivityPackage.class);
        when(pkg.getPackageTitle()).thenReturn("2025년 3월 영업활동 보고서");

        // ── 패키지 아이템 4개 (MEETING / ISSUE / SCHEDULE / MEMO) ────
        ActivityPackageItem item1 = mock(ActivityPackageItem.class);
        when(item1.getActivityId()).thenReturn(1L);

        ActivityPackageItem item2 = mock(ActivityPackageItem.class);
        when(item2.getActivityId()).thenReturn(2L);

        ActivityPackageItem item3 = mock(ActivityPackageItem.class);
        when(item3.getActivityId()).thenReturn(3L);

        ActivityPackageItem item4 = mock(ActivityPackageItem.class);
        when(item4.getActivityId()).thenReturn(4L);

        when(pkg.getItems()).thenReturn(List.of(item1, item2, item3, item4));

        // ── 활동 데이터 mock 설정 ────────────────────────────────────
        // 1. MEETING
        when(activityQueryService.getActivity(1L)).thenReturn(new ActivityResponse(
                1L, 10L, "PO-2025-001", 7L,
                LocalDate.of(2025, 3, 5),
                ActivityType.MEETING,
                "1분기 영업 전략 미팅",
                "거래처 A사 방문, 신규 계약 조건 협의 및 샘플 전달",
                Priority.HIGH,
                null, null, null, null,
                "김영업", "A무역"
        ));

        // 2. ISSUE
        when(activityQueryService.getActivity(2L)).thenReturn(new ActivityResponse(
                2L, 10L, "PO-2025-001", 7L,
                LocalDate.of(2025, 3, 10),
                ActivityType.ISSUE,
                "불량률 이슈 접수",
                "A사에서 전달받은 샘플 중 3% 불량 발생. 품질팀 검토 요청",
                Priority.HIGH,
                null, null, null, null,
                "김영업", "A무역"
        ));

        // 3. SCHEDULE (시작일·종료일 있음)
        when(activityQueryService.getActivity(3L)).thenReturn(new ActivityResponse(
                3L, 10L, "PO-2025-001", 7L,
                LocalDate.of(2025, 3, 12),
                ActivityType.SCHEDULE,
                "제품 납기 관리 일정",
                "생산 라인 점검 및 선적 일정 조율",
                Priority.NORMAL,
                LocalDate.of(2025, 3, 12),
                LocalDate.of(2025, 3, 25),
                null, null,
                "김영업", "A무역"
        ));

        // 4. MEMO
        when(activityQueryService.getActivity(4L)).thenReturn(new ActivityResponse(
                4L, 10L, null, 7L,
                LocalDate.of(2025, 3, 20),
                ActivityType.MEMO,
                "클레임 대응 내용 메모",
                "불량 원인 분석 완료. 다음 로트부터 공정 개선 적용 예정",
                null,
                null, null, null, null,
                "이담당", "A무역"
        ));

        // ── 작성자 이름 mock 설정 ────────────────────────────────────
        UserResponse creator = mock(UserResponse.class);
        when(creator.getName()).thenReturn("박과장");
        when(authFeignClient.getUser(7L)).thenReturn(creator);

        // ── PDF 생성 ─────────────────────────────────────────────────
        byte[] pdf = pdfReportService.generatePackageReport(pkg, 7L);

        // 패키지 제목 기반 파일명을 가져온 뒤 확장자(.pdf)를 제거해 baseName으로 사용한다.
        String downloadFileName = pdfReportService.getDownloadFileName(pkg); // "2025년 3월 영업활동 보고서.pdf"
        String baseName = downloadFileName.substring(0, downloadFileName.lastIndexOf('.'));
        // 다운로드 폴더에 중복되지 않는 파일 경로를 결정한다.
        Path outputPath = resolveUniqueFilePath(
                Path.of(System.getProperty("user.home"), "Downloads"), baseName);
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            fos.write(pdf);
        }

        System.out.println("✅ PDF 생성 완료: " + outputPath.toAbsolutePath());
    }

    // 지정한 폴더에서 중복되지 않는 파일 경로를 반환한다.
    // 파일이 이미 존재하면 sample-report(1).pdf, sample-report(2).pdf 순으로 번호를 붙인다.
    private Path resolveUniqueFilePath(Path directory, String baseName) {
        // 번호 없이 기본 파일명으로 먼저 시도한다.
        Path candidate = directory.resolve(baseName + ".pdf");
        int counter = 1;
        // 파일이 이미 존재하는 동안 번호를 올려가며 후보 경로를 다시 만든다.
        while (Files.exists(candidate)) {
            candidate = directory.resolve(baseName + "(" + counter + ").pdf");
            counter++;
        }
        return candidate;
    }
}
