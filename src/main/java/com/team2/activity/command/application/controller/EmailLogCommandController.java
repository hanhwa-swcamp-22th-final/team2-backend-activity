package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.EmailLogCommandService;
import com.team2.activity.command.application.dto.EmailLogCreateRequest;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.query.controller.EmailLogQueryController;
import com.team2.activity.query.dto.EmailLogResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
public class EmailLogCommandController {

    private final EmailLogCommandService emailLogCommandService;

    @PostMapping
    public ResponseEntity<EntityModel<EmailLogResponse>> createEmailLog(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody EmailLogCreateRequest request) {
        EmailLog emailLog = emailLogCommandService.createEmailLog(request.toEntity(userId));
        EntityModel<EmailLogResponse> model = EntityModel.of(EmailLogResponse.from(emailLog),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLog.getEmailLogId())).withSelfRel(),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLogs(null, null)).withRel("email-logs"));
        URI location = linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLog.getEmailLogId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PostMapping("/{emailLogId}/resend")
    public ResponseEntity<EntityModel<EmailLogResponse>> resend(@PathVariable Long emailLogId) {
        EmailLogResponse response = EmailLogResponse.from(emailLogCommandService.resend(emailLogId));
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLogId)).withSelfRel(),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLogs(null, null)).withRel("email-logs")));
    }
}
