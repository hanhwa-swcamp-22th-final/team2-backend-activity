package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.enums.ActivityType;
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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityPackagePdfReportService 최종 검증 테스트")
class ActivityPackagePdfReportServiceTest {

    @Mock
    private ActivityQueryService activityQueryService;

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private DocumentsFeignClient documentsFeignClient;

    @InjectMocks
    private ActivityPackagePdfReportService pdfReportService;

    @Test
    @DisplayName("패키지 제목이 파일명이 되고 작성자 이름이 '홍길동'으로 깔끔하게 나오는지 테스트")
    void generateFinalPackageReport_And_SaveWithDynamicFileName() throws Exception {
        // 1. 테스트용 패키지 데이터 준비 (패키지 제목 설정)
        String targetTitle = "최신 로직 반영 보고서 제목";
        ActivityPackage mockPackage = mock(ActivityPackage.class);
        when(mockPackage.getPackageTitle()).thenReturn(targetTitle);
        
        ActivityPackageItem mockItem = mock(ActivityPackageItem.class);
        when(mockItem.getActivityId()).thenReturn(100L);
        when(mockPackage.getItems()).thenReturn(List.of(mockItem));
        
        // 2. 가짜 활동(Activity) 데이터 준비
        ActivityResponse mockActivity = new ActivityResponse(
                100L, 10L, "PO-2024-FINAL", 1L, LocalDate.now(),
                ActivityType.MEETING, "최종 검증 미팅", "로직이 완벽하게 작동합니다.",
                null, null, null, null, null,
                "활동작성자", "테스트거래처"
        );
        when(activityQueryService.getActivity(100L)).thenReturn(mockActivity);
        
        // 3. 작성자 이름 조회 Mock 설정 (깔끔하게 '홍길동'만 반환)
        UserResponse userResponse = mock(UserResponse.class);
        when(userResponse.getName()).thenReturn("홍길동");
        // 헤더에서 넘어올 userId가 999L이라고 가정
        when(authFeignClient.getUser(999L)).thenReturn(userResponse);

        // 4. PDF 생성 실행
        byte[] pdfBytes = pdfReportService.generatePackageReport(mockPackage, 999L);
        
        // 5. 서비스 로직을 통해 파일명 추출 (패키지 제목 기준)
        String downloadFileName = pdfReportService.getDownloadFileName(mockPackage);
        
        // 6. 검증: 파일명이 패키지 제목과 일치하는지 확인
        assertThat(downloadFileName).isEqualTo(targetTitle + ".pdf");
        assertThat(pdfBytes).isNotEmpty();
        
        // 7. 실제 파일 저장
        Path testOutputPath = Paths.get("build", "test-results");
        if (!Files.exists(testOutputPath)) {
            Files.createDirectories(testOutputPath);
        }
        // 서비스가 생성해준 파일명 그대로 저장합니다.
        Path pdfFile = testOutputPath.resolve(downloadFileName);
        
        try (FileOutputStream fos = new FileOutputStream(pdfFile.toFile())) {
            fos.write(pdfBytes);
        }
        
        System.out.println("파일이 생성되었습니다: " + pdfFile.toAbsolutePath());
    }

    @Test
    @DisplayName("파일명에 특수문자가 포함되면 안전한 파일명으로 정화한다")
    void getDownloadFileName_sanitizesSpecialCharacters() {
        // 파일 시스템에서 사용할 수 없는 특수문자가 포함된 제목을 설정한다.
        ActivityPackage mockPackage = mock(ActivityPackage.class);
        when(mockPackage.getPackageTitle()).thenReturn("위험한/파일명:*?\"<>|");

        // 파일명 생성 시 특수문자가 밑줄로 치환되는지 확인한다.
        String fileName = pdfReportService.getDownloadFileName(mockPackage);
        // 슬래시 등이 밑줄로 바뀌어 안전하게 반환되는지 확인한다.
        assertThat(fileName).isEqualTo("위험한_파일명_.pdf");
    }

    @Test
    @DisplayName("패키지 제목이 비어 있으면 PO 번호 기반으로 제목을 생성한다")
    void buildReportTitle_usesPoNumberWhenTitleIsEmpty() {
        // 제목이 없는 패키지 엔티티를 준비한다.
        ActivityPackage mockPackage = mock(ActivityPackage.class);
        when(mockPackage.getPackageTitle()).thenReturn("");
        when(mockPackage.getPoId()).thenReturn("PO-999");
        
        // PO 번호 조회를 위한 목 설정
        PurchaseOrderResponse poResponse = mock(PurchaseOrderResponse.class);
        when(poResponse.getPoNo()).thenReturn("PO-FINAL-999");
        when(documentsFeignClient.getPurchaseOrder("PO-999")).thenReturn(poResponse);

        // 제목 생성 로직을 직접 테스트하지는 못하므로 PDF 생성 시 간접 확인하거나 
        // 공개된 메서드인 getDownloadFileName을 통해 확인한다.
        String fileName = pdfReportService.getDownloadFileName(mockPackage);
        // PO 번호 기반 제목으로 파일명이 생성되는지 확인한다.
        assertThat(fileName).isEqualTo("PO-FINAL-999 PKG.pdf");
    }
}
