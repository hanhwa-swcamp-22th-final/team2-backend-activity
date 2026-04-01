package com.team2.activity.command.controller;

import com.team2.activity.command.service.EmailLogCommandService;
import com.team2.activity.dto.EmailLogCreateRequest;
import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.EmailLogType;
import com.team2.activity.entity.enums.MailStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
public class EmailLogCommandController {

    private final EmailLogCommandService emailLogCommandService;

    @PostMapping
    public ResponseEntity<EmailLog> createEmailLog(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody EmailLogCreateRequest request) {
        List<EmailLogType> docTypes = request.docTypes() != null
                ? request.docTypes().stream().map(EmailLogType::of).toList()
                : List.of();
        EmailLog emailLog = EmailLog.builder()
                .clientId(request.clientId())
                .poId(request.poId())
                .emailTitle(request.emailTitle())
                .emailRecipientName(request.emailRecipientName())
                .emailRecipientEmail(request.emailRecipientEmail())
                .emailSenderId(userId)
                .emailStatus(MailStatus.SENT)
                .docTypes(docTypes)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emailLogCommandService.createEmailLog(emailLog));
    }

    @PostMapping("/{emailLogId}/resend")
    public ResponseEntity<EmailLog> resend(@PathVariable Long emailLogId) {
        return ResponseEntity.ok(emailLogCommandService.resend(emailLogId));
    }
}
