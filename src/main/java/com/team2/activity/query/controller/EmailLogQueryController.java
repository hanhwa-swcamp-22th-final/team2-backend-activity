package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import com.team2.activity.query.service.EmailLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
public class EmailLogQueryController {

    private final EmailLogQueryService emailLogQueryService;

    @GetMapping
    public ResponseEntity<PagedResponse<EmailLog>> getEmailLogs(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) MailStatus emailStatus) {
        List<EmailLog> logs;
        if (clientId != null) {
            logs = emailLogQueryService.getEmailLogsByClientId(clientId);
        } else if (emailStatus != null) {
            logs = emailLogQueryService.getEmailLogsByStatus(emailStatus);
        } else {
            logs = emailLogQueryService.getAllEmailLogs();
        }
        return ResponseEntity.ok(PagedResponse.of(logs));
    }

    @GetMapping("/{emailLogId}")
    public ResponseEntity<EmailLog> getEmailLog(@PathVariable Long emailLogId) {
        return ResponseEntity.ok(emailLogQueryService.getEmailLog(emailLogId));
    }
}
