package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.query.dto.EmailLogResponse;
import com.team2.activity.query.service.EmailLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
public class EmailLogQueryController {

    private final EmailLogQueryService emailLogQueryService;

    @GetMapping
    public ResponseEntity<PagedResponse<EmailLogResponse>> getEmailLogs(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String poId,
            @RequestParam(required = false) MailStatus emailStatus,
            @RequestParam(required = false) Long emailSenderId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDateTime dateTimeFrom = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime dateTimeTo = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        List<EmailLogResponse> responses = emailLogQueryService.getEmailLogsWithFilters(
                clientId, poId, emailStatus, emailSenderId, keyword, dateTimeFrom, dateTimeTo);
        return ResponseEntity.ok(PagedResponse.of(responses, page, size));
    }

    @GetMapping("/{emailLogId}")
    public ResponseEntity<EmailLogResponse> getEmailLog(@PathVariable Long emailLogId) {
        return ResponseEntity.ok(emailLogQueryService.getEmailLog(emailLogId));
    }
}
