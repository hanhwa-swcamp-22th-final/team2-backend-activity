package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.query.dto.EmailLogResponse;
import com.team2.activity.query.service.EmailLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "이메일 로그 Query", description = "이메일 로그 조회 API")
@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
public class EmailLogQueryController {

    private final EmailLogQueryService emailLogQueryService;

    @Operation(summary = "이메일 로그 목록 조회", description = "필터 조건에 따라 이메일 로그 목록을 페이징 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<EmailLogResponse>> getEmailLogs(
            @Parameter(description = "거래처 ID") @RequestParam(required = false) Long clientId,
            @Parameter(description = "PO ID") @RequestParam(required = false) String poId,
            @Parameter(description = "메일 발송 상태") @RequestParam(required = false) MailStatus emailStatus,
            @Parameter(description = "발송자 ID") @RequestParam(required = false) Long emailSenderId,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {
        LocalDateTime dateTimeFrom = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime dateTimeTo = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        List<EmailLogResponse> responses = emailLogQueryService.getEmailLogsWithFilters(
                clientId, poId, emailStatus, emailSenderId, keyword, dateTimeFrom, dateTimeTo, page, size);
        long totalElements = emailLogQueryService.countWithFilters(
                clientId, poId, emailStatus, emailSenderId, keyword, dateTimeFrom, dateTimeTo);
        return ResponseEntity.ok(PagedResponse.of(responses, totalElements, page, size));
    }

    @Operation(summary = "이메일 로그 상세 조회", description = "이메일 로그 ID로 상세 정보를 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "이메일 로그를 찾을 수 없음")
    })
    @GetMapping("/{emailLogId}")
    public ResponseEntity<EmailLogResponse> getEmailLog(
            @Parameter(description = "이메일 로그 ID", required = true) @PathVariable Long emailLogId) {
        return ResponseEntity.ok(emailLogQueryService.getEmailLog(emailLogId));
    }
}
