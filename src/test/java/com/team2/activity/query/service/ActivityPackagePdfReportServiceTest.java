package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.PurchaseOrderResponse;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityPackagePdfReportServiceTest {

    @Mock
    private ActivityPackageQueryService activityPackageQueryService;

    @Mock
    private ActivityQueryService activityQueryService;

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private DocumentsFeignClient documentsFeignClient;

    @InjectMocks
    private ActivityPackagePdfReportService pdfReportService;

    @Test
    @DisplayName("활동 패키지를 PDF로 변환하여 실제 파일로 저장 테스트")
    void generatePackageReport_Success_And_SaveFile() throws Exception {
        // 1. 가짜 패키지 데이터 준비
        ActivityPackage mockPackage = mock(ActivityPackage.class);
        ActivityPackageItem mockItem = mock(ActivityPackageItem.class);
        
        when(mockPackage.getPoId()).thenReturn("PO-2024-001");
        when(mockPackage.getCreatorId()).thenReturn(1L);
        when(mockPackage.getItems()).thenReturn(List.of(mockItem));
        when(mockItem.getActivityId()).thenReturn(100L);
        
        when(activityPackageQueryService.getPackage(anyLong())).thenReturn(mockPackage);
        
        // 2. 가짜 활동(Activity) 데이터 준비
        ActivityResponse mockActivity = new ActivityResponse(
                100L, 10L, "PO-2024-001", 1L, LocalDate.now(),
                ActivityType.MEETING, "테스트 미팅 제목", "테스트 내용입니다.",
                null, null, null, null, null,
                "작성자이름", "거래처이름"
        );
        when(activityQueryService.getActivity(100L)).thenReturn(mockActivity);
        
        // 3. 외부 서비스 Mock 응답 설정
        PurchaseOrderResponse poResponse = mock(PurchaseOrderResponse.class);
        when(poResponse.getPoNo()).thenReturn("PO-ABC-123");
        when(documentsFeignClient.getPurchaseOrder(anyString())).thenReturn(poResponse);
        
        UserResponse userResponse = mock(UserResponse.class);
        when(userResponse.getName()).thenReturn("홍길동");
        when(authFeignClient.getUser(anyLong())).thenReturn(userResponse);

        // 4. PDF 생성 실행
        byte[] pdfBytes = pdfReportService.generatePackageReport(1L);

        // 5. 검증
        assertNotNull(pdfBytes, "생성된 PDF 바이트 배열은 null이 아니어야 합니다.");
        assertTrue(pdfBytes.length > 0, "생성된 PDF 파일 크기는 0보다 커야 합니다.");

        // 6. 실제 파일로 저장 (눈으로 확인하기 위함)
        Path testOutputPath = Paths.get("build", "test-results");
        if (!Files.exists(testOutputPath)) {
            Files.createDirectories(testOutputPath);
        }
        Path pdfFile = testOutputPath.resolve("activity-package-test-report.pdf");
        
        try (FileOutputStream fos = new FileOutputStream(pdfFile.toFile())) {
            fos.write(pdfBytes);
        }
        
        System.out.println("PDF 파일이 성공적으로 생성되었습니다: " + pdfFile.toAbsolutePath());
    }
}
