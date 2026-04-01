package com.team2.activity.command.controller;

import com.team2.activity.command.service.EmailLogCommandService;
import com.team2.activity.dto.EmailLogCreateRequest;
import com.team2.activity.entity.EmailLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
public class EmailLogCommandController {

    private final EmailLogCommandService emailLogCommandService;

    @PostMapping
    public ResponseEntity<EmailLog> createEmailLog(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody EmailLogCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emailLogCommandService.createEmailLog(request.toEntity(userId)));
    }

    @PostMapping("/{emailLogId}/resend")
    public ResponseEntity<EmailLog> resend(@PathVariable Long emailLogId) {
        return ResponseEntity.ok(emailLogCommandService.resend(emailLogId));
    }
}
