package com.team2.activity.query.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.team2.activity.command.domain.entity.ActivityPackage;
import com.team2.activity.command.domain.entity.ActivityPackageItem;
import com.team2.activity.command.domain.entity.enums.ActivityType;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.PurchaseOrderResponse;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.query.dto.ActivityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// 활동 패키지 데이터를 PDF 보고서로 생성하는 query service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 읽기 전용 트랜잭션으로 조회 성격을 명확히 한다.
@Transactional(readOnly = true)
public class ActivityPackagePdfReportService {

    // PDF 본문 날짜 표시에 사용할 포맷터다.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    // macOS에서 우선 사용할 Apple SD Gothic Neo 폰트 후보 경로다.
    private static final String MAC_KOREAN_FONT_COLLECTION_PATH = "/System/Library/Fonts/AppleSDGothicNeo.ttc";
    // macOS에서 차선으로 사용할 명조 계열 한글 폰트 후보 경로다.
    private static final String MAC_KOREAN_FONT_PATH = "/System/Library/Fonts/Supplemental/AppleMyungjo.ttf";
    // Windows 환경에서 사용할 맑은 고딕 폰트 경로다.
    private static final String WIN_KOREAN_FONT_PATH = "C:/Windows/Fonts/malgun.ttf";
    // Linux 환경에서 자주 사용하는 Nanum 폰트 후보 경로다.
    private static final String LINUX_NANUM_FONT_PATH = "/usr/share/fonts/truetype/nanum/NanumGothic.ttf";
    // Linux 환경에서 자주 사용하는 Noto CJK 폰트 후보 경로다.
    private static final String LINUX_NOTO_FONT_PATH = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc";

    // PKG에 포함된 Activity 상세 조회 로직을 재사용한다.
    private final ActivityQueryService activityQueryService;
    // 작성자 이름 조회를 위해 인증 서비스 클라이언트를 재사용한다.
    private final AuthFeignClient authFeignClient;
    // PO 번호 조회를 위해 문서 서비스 클라이언트를 재사용한다.
    private final DocumentsFeignClient documentsFeignClient;

    // 운영 환경에서 직접 지정할 수 있는 PDF 폰트 경로다.
    @Value("${report.pdf.font-path:}")
    private String configuredFontPath;

    // 패키지 엔티티와 현재 요청자 ID를 기준으로 PDF 보고서 바이트 배열을 생성한다.
    public byte[] generatePackageReport(ActivityPackage activityPackage, Long userId) {
        // 제목에 사용할 PO 표시값을 만든다.
        String title = buildReportTitle(activityPackage);
        // 제목 아래에 표시할 작성자 이름을 만든다 (헤더 ID 우선, 없으면 패키지 생성자 ID).
        String creatorName = resolveCreatorName(activityPackage, userId);
        // PKG item 순서대로 Activity 상세 데이터를 읽어 온다.
        List<ActivityResponse> activities = loadPackageActivities(activityPackage);
        // 수집한 데이터를 기준으로 PDF 문서를 생성한다.
        return buildPdf(title, creatorName, activities);
    }

    // 패키지 제목을 기준으로 다운로드용 PDF 파일명을 생성한다.
    public String getDownloadFileName(ActivityPackage activityPackage) {
        // 보고서 제목 규칙과 동일한 기준으로 파일명 기본값을 만든다.
        String reportTitle = buildReportTitle(activityPackage);
        // 파일명에 사용할 수 없는 문자를 안전한 문자로 치환한다.
        String sanitizedTitle = sanitizeFileName(reportTitle);
        // 다운로드 시 PDF 확장자가 붙은 파일명을 반환한다.
        return sanitizedTitle + ".pdf";
    }

    // 패키지에 포함된 Activity ID 순서대로 상세 데이터를 조회한다.
    private List<ActivityResponse> loadPackageActivities(ActivityPackage activityPackage) {
        // item 컬렉션을 순회하며 기존 Activity 단건 조회 로직을 재사용한다.
        return activityPackage.getItems().stream()
                // 각 item의 activityId로 Activity 상세 데이터를 읽는다.
                .map(ActivityPackageItem::getActivityId)
                // 기존 상세 조회 로직이 작성자 이름 enrichment까지 수행한다.
                .map(activityQueryService::getActivity)
                // 조회한 Activity DTO들을 리스트로 모은다.
                .toList();
    }

    // 패키지 제목을 우선 사용하고 없으면 PO 번호 + PKG 형식의 보고서 제목을 만든다.
    private String buildReportTitle(ActivityPackage activityPackage) {
        // 사용자가 입력한 패키지 제목이 있으면 그 값을 문서 제목으로 사용한다.
        if (activityPackage.getPackageTitle() != null && !activityPackage.getPackageTitle().isBlank()) {
            return activityPackage.getPackageTitle().trim();
        }
        // PO 번호 조회 결과가 있으면 그 값을 사용한다.
        String poNumber = fetchPurchaseOrderNumber(activityPackage.getPoId());
        // 제목 규칙에 맞춰 "PO번호 PKG" 형식으로 조합한다.
        return poNumber + " PKG";
    }

    // 헤더에서 받은 userId 또는 패키지 작성자 ID로 사용자 이름을 조회한다.
    private String resolveCreatorName(ActivityPackage activityPackage, Long userId) {
        // 헤더에서 넘어온 요청자 ID가 있으면 우선적으로 그 이름을 조회한다.
        Long targetId = (userId != null) ? userId : activityPackage.getCreatorId();
        // 대상 ID를 기준으로 인증 서비스에서 이름을 조회한다.
        String creatorName = fetchUserName(targetId);
        // 조회 성공 시 이름을 그대로 사용한다.
        if (creatorName != null && !creatorName.isBlank()) {
            return creatorName;
        }
        // 이름 조회 실패 시 대상 ID 문자열을 대체값으로 사용한다.
        if (targetId != null) {
            return String.valueOf(targetId);
        }
        // 대상 ID도 전혀 없으면 알 수 없음 문자열을 사용한다.
        return "-";
    }

    // 문서 서비스에서 PO 번호를 조회하고 실패 시 poId를 대체값으로 사용한다.
    private String fetchPurchaseOrderNumber(String poId) {
        // poId 자체가 없으면 제목용 대체값을 반환한다.
        if (poId == null || poId.isBlank()) {
            return "-";
        }
        try {
            // 문서 서비스에서 발주 정보를 조회한다.
            PurchaseOrderResponse purchaseOrder = documentsFeignClient.getPurchaseOrder(poId);
            // PO 번호가 있으면 화면 표시용 번호를 사용한다.
            if (purchaseOrder != null && purchaseOrder.getPoNo() != null && !purchaseOrder.getPoNo().isBlank()) {
                return purchaseOrder.getPoNo();
            }
        } catch (Exception ignored) {
            // 외부 서비스 실패 시에도 PDF 생성은 계속 진행한다.
        }
        // 문서 서비스 조회가 실패하면 원본 poId를 대체값으로 사용한다.
        return poId;
    }

    // 인증 서비스에서 사용자 이름을 조회하고 실패 시 null을 반환한다.
    private String fetchUserName(Long userId) {
        // userId가 없으면 조회하지 않는다.
        if (userId == null) {
            return null;
        }
        try {
            // 인증 서비스에서 사용자 정보를 조회한다.
            UserResponse user = authFeignClient.getUser(userId);
            // 조회 성공 시 사용자 이름을 반환한다.
            if (user != null && user.getName() != null && !user.getName().isBlank()) {
                return user.getName();
            }
        } catch (Exception ignored) {
            // 외부 서비스 실패 시에도 PDF 생성은 계속 진행한다.
        }
        // 이름을 조회하지 못한 경우 null을 반환한다.
        return null;
    }

    // 수집한 제목, 작성자, 활동 목록을 사용해 실제 PDF 바이트 배열을 만든다.
    private byte[] buildPdf(String title, String creatorName, List<ActivityResponse> activities) {
        // 가로 폭을 넉넉히 쓰기 위해 A4 가로 방향 문서를 생성한다.
        Document document = new Document(PageSize.A4.rotate(), 36f, 36f, 36f, 36f);
        
        // PDF 결과를 메모리에 쌓아 둘 출력 스트림을 준비한다.
        // try-with-resources를 사용하여 스트림이 안전하게 닫히도록 보장한다.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // PDF 작성기(Writer)를 문서와 출력 스트림에 연결한다.
            PdfWriter.getInstance(document, outputStream);
            // 문서 쓰기를 시작한다.
            document.open();
            // PDF 메타데이터 제목도 문서 상단 제목과 같은 값으로 기록한다.
            document.addTitle(title);
            
            // 한글 출력을 위해 시스템 폰트를 로드한다.
            BaseFont baseFont = loadBaseFont();
            // 제목용 폰트 (18pt, 굵게)
            Font titleFont = new Font(baseFont, 18f, Font.BOLD);
            // 작성자 정보용 폰트 (11pt, 보통)
            Font authorFont = new Font(baseFont, 11f, Font.NORMAL);
            // 테이블 헤더용 폰트 (10pt, 굵게, 흰색)
            Font headerFont = new Font(baseFont, 10f, Font.BOLD, Color.WHITE);
            // 테이블 본문용 폰트 (10pt, 보통)
            Font bodyFont = new Font(baseFont, 10f, Font.NORMAL);
            
            // 중앙 정렬된 제목 문단을 생성하여 추가한다.
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(6f);
            document.add(titleParagraph);
            
            // 우측 정렬된 작성자 문단을 생성하여 추가한다.
            Paragraph authorParagraph = new Paragraph("작성자: " + creatorName, authorFont);
            authorParagraph.setAlignment(Element.ALIGN_RIGHT);
            authorParagraph.setSpacingAfter(14f);
            document.add(authorParagraph);
            
            // 활동 내역을 표시할 7열 테이블을 생성한다.
            PdfPTable table = new PdfPTable(7);
            // 테이블 너비를 페이지 가득 채운다.
            table.setWidthPercentage(100f);
            // 각 열의 너비 비율을 설정한다.
            table.setWidths(new float[]{0.9f, 1.5f, 3.6f, 1.6f, 1.8f, 1.5f, 1.5f});
            // 첫 번째 행을 헤더로 지정하여 페이지가 넘어가도 반복되게 한다.
            table.setHeaderRows(1);
            // 페이지 끝에서 행이 잘리지 않도록 설정한다.
            table.setSplitLate(false);
            // 테이블 상단 여백 설정
            table.setSpacingBefore(4f);
            
            // 테이블 헤더 셀들을 추가한다.
            String[] headers = {"항목", "유형", "제목", "작성일", "작성자", "시작일", "종료일"};
            for (String headerText : headers) {
                addHeaderCell(table, headerText, headerFont);
            }
            
            // 활동 목록(activities)을 순회하며 테이블 행을 채운다.
            for (int index = 0; index < activities.size(); index++) {
                ActivityResponse activity = activities.get(index);
                // 연번
                addBodyCell(table, String.valueOf(index + 1), bodyFont, Element.ALIGN_CENTER);
                // 활동 유형 (미팅, 이슈 등)
                addBodyCell(table, resolveTypeLabel(activity.activityType()), bodyFont, Element.ALIGN_CENTER);
                // 제목
                addBodyCell(table, safeText(activity.activityTitle()), bodyFont, Element.ALIGN_LEFT);
                // 작성일
                addBodyCell(table, formatDate(activity.activityDate()), bodyFont, Element.ALIGN_CENTER);
                // 작성자
                addBodyCell(table, resolveActivityWriter(activity), bodyFont, Element.ALIGN_CENTER);
                // 일정 시작일
                addBodyCell(table, resolveScheduleStartDate(activity), bodyFont, Element.ALIGN_CENTER);
                // 일정 종료일
                addBodyCell(table, resolveScheduleEndDate(activity), bodyFont, Element.ALIGN_CENTER);
            }
            
            // 완성된 테이블을 문서에 추가한다.
            document.add(table);
            
            // 문서 작성을 마치고 닫는다. (이 시점에 스트림에 모든 데이터가 쓰인다.)
            document.close();
            
            // 생성된 PDF 바이트 배열을 반환한다.
            return outputStream.toByteArray();
        } catch (IOException | RuntimeException e) {
            // 오류 발생 시 문서가 열려 있다면 닫아서 리소스를 해제한다.
            if (document.isOpen()) {
                document.close();
            }
            // 예외 원인을 포함해 PDF 생성 실패 예외를 던진다.
            throw new IllegalStateException("활동 패키지 PDF 보고서를 생성할 수 없습니다.", e);
        }
    }

    // 헤더 스타일이 적용된 셀을 표에 추가한다.
    private void addHeaderCell(PdfPTable table, String text, Font font) {
        // 헤더 텍스트를 담은 셀을 생성한다.
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        // 헤더 배경을 진한 회색으로 설정한다.
        cell.setBackgroundColor(new Color(84, 98, 117));
        // 헤더 텍스트를 가운데 정렬한다.
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        // 헤더 텍스트를 수직 가운데 정렬한다.
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        // 헤더 내부 여백을 준다.
        cell.setPadding(8f);
        // 셀 테두리 색을 연한 회색으로 설정한다.
        cell.setBorderColor(new Color(210, 210, 210));
        // 완성된 헤더 셀을 표에 추가한다.
        table.addCell(cell);
    }

    // 본문 스타일이 적용된 셀을 표에 추가한다.
    private void addBodyCell(PdfPTable table, String text, Font font, int horizontalAlignment) {
        // 본문 텍스트를 담은 셀을 생성한다.
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        // 호출자가 지정한 정렬값을 적용한다.
        cell.setHorizontalAlignment(horizontalAlignment);
        // 본문 텍스트를 수직 가운데 정렬한다.
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        // 본문 내부 여백을 준다.
        cell.setPadding(7f);
        // 셀 테두리 색을 연한 회색으로 설정한다.
        cell.setBorderColor(new Color(220, 220, 220));
        // 셀 최소 높이를 지정해 가독성을 높인다.
        cell.setMinimumHeight(24f);
        // 완성된 본문 셀을 표에 추가한다.
        table.addCell(cell);
    }

    // ActivityType 값을 PDF 표에 표시할 한글 라벨로 변환한다.
    private String resolveTypeLabel(ActivityType activityType) {
        // 값이 없으면 하이픈을 반환한다.
        if (activityType == null) {
            return "-";
        }
        // enum 값에 맞는 한글 라벨을 반환한다.
        return switch (activityType) {
            // 미팅 유형은 미팅으로 표시한다.
            case MEETING -> "미팅";
            // 이슈 유형은 이슈로 표시한다.
            case ISSUE -> "이슈";
            // 메모 유형은 메모로 표시한다.
            case MEMO -> "메모";
            // 일정 유형은 일정으로 표시한다.
            case SCHEDULE -> "일정";
        };
    }

    // Activity 작성자명을 안전하게 문자열로 변환한다.
    private String resolveActivityWriter(ActivityResponse activity) {
        // enrichment된 작성자 이름이 있으면 우선 사용한다.
        if (activity.authorName() != null && !activity.authorName().isBlank()) {
            return activity.authorName();
        }
        // 작성자 ID가 있으면 문자열로 변환해 대체값으로 사용한다.
        if (activity.activityAuthorId() != null) {
            return String.valueOf(activity.activityAuthorId());
        }
        // 작성자 정보가 전혀 없으면 하이픈을 반환한다.
        return "-";
    }

    // 일정 유형일 때만 시작일을 표시하고 나머지는 하이픈을 반환한다.
    private String resolveScheduleStartDate(ActivityResponse activity) {
        // 유형이 일정이면 실제 시작일을 포맷한다.
        if (activity.activityType() == ActivityType.SCHEDULE) {
            return formatDate(activity.activityScheduleFrom());
        }
        // 일정이 아니면 하이픈을 반환한다.
        return "-";
    }

    // 일정 유형일 때만 종료일을 표시하고 나머지는 하이픈을 반환한다.
    private String resolveScheduleEndDate(ActivityResponse activity) {
        // 유형이 일정이면 실제 종료일을 포맷한다.
        if (activity.activityType() == ActivityType.SCHEDULE) {
            return formatDate(activity.activityScheduleTo());
        }
        // 일정이 아니면 하이픈을 반환한다.
        return "-";
    }

    // LocalDate 값을 YYYY-MM-DD 형식 문자열로 변환한다.
    private String formatDate(LocalDate date) {
        // 날짜 값이 있으면 ISO 포맷으로 문자열을 만든다.
        if (date != null) {
            return DATE_FORMATTER.format(date);
        }
        // 날짜 값이 없으면 하이픈을 반환한다.
        return "-";
    }

    // null 또는 공백 문자열을 PDF 출력용 안전한 텍스트로 바꾼다.
    private String safeText(String value) {
        // 실제 텍스트가 있으면 그대로 사용한다.
        if (value != null && !value.isBlank()) {
            return value;
        }
        // 텍스트가 없으면 하이픈을 반환한다.
        return "-";
    }

    // 운영체제 파일명에 사용할 수 없는 문자를 안전한 문자로 치환한다.
    private String sanitizeFileName(String fileName) {
        // 줄바꿈과 파일 경로 예약 문자를 밑줄로 치환해 안전한 파일명을 만든다.
        String sanitizedFileName = fileName.replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_").trim();
        // 치환 결과가 비어 있지 않으면 그 값을 그대로 사용한다.
        if (!sanitizedFileName.isBlank()) {
            return sanitizedFileName;
        }
        // 치환 후 비어 있으면 안전한 기본 파일명을 사용한다.
        return "activity-package-report";
    }

    // PDF에 사용할 한글 BaseFont를 로드한다.
    private BaseFont loadBaseFont() throws IOException, DocumentException {
        // 설정값 또는 기본 후보 경로 중 실제 존재하는 폰트 경로를 찾는다.
        String fontPath = resolveFontPath();
        // TTC 컬렉션 폰트면 첫 번째 폰트 인덱스를 사용하도록 이름을 보정한다.
        String baseFontName = fontPath.endsWith(".ttc") ? fontPath + ",0" : fontPath;
        // 폰트 파일 경로를 직접 사용해 한글 지원 BaseFont를 생성한다.
        return BaseFont.createFont(baseFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    }

    // 설정값과 운영체제 기본 경로를 순서대로 검사해 사용 가능한 폰트 경로를 찾는다.
    private String resolveFontPath() {
        // 설정값이 있고 실제 파일이 존재하면 최우선으로 사용한다.
        if (configuredFontPath != null && !configuredFontPath.isBlank() && Files.exists(Path.of(configuredFontPath))) {
            return configuredFontPath;
        }
        // macOS Apple SD Gothic Neo 컬렉션 폰트가 있으면 그 경로를 사용한다.
        if (Files.exists(Path.of(MAC_KOREAN_FONT_COLLECTION_PATH))) {
            return MAC_KOREAN_FONT_COLLECTION_PATH;
        }
        // macOS 기본 한글 폰트가 있으면 그 경로를 사용한다.
        if (Files.exists(Path.of(MAC_KOREAN_FONT_PATH))) {
            return MAC_KOREAN_FONT_PATH;
        }
        // Windows 맑은 고딕 폰트가 있으면 그 경로를 사용한다.
        if (Files.exists(Path.of(WIN_KOREAN_FONT_PATH))) {
            return WIN_KOREAN_FONT_PATH;
        }
        // Linux Nanum 폰트가 있으면 그 경로를 사용한다.
        if (Files.exists(Path.of(LINUX_NANUM_FONT_PATH))) {
            return LINUX_NANUM_FONT_PATH;
        }
        // Linux Noto CJK 폰트가 있으면 그 경로를 사용한다.
        if (Files.exists(Path.of(LINUX_NOTO_FONT_PATH))) {
            return LINUX_NOTO_FONT_PATH;
        }
        // 사용할 수 있는 한글 폰트가 없으면 명시적 예외를 던진다.
        throw new IllegalStateException("PDF 생성에 사용할 한글 폰트를 찾을 수 없습니다. (검사 경로: macOS, Windows, Linux 기본 경로)");
    }
}
