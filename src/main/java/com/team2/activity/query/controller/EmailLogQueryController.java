package com.team2.activity.query.controller;

import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.query.dto.EmailLogResponse;
import com.team2.activity.query.service.EmailLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
public class EmailLogQueryController {

    private final EmailLogQueryService emailLogQueryService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<EmailLogResponse>>> getEmailLogs(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) MailStatus emailStatus) {
        List<EmailLogResponse> logs = (clientId != null
                ? emailLogQueryService.getEmailLogsByClientId(clientId)
                : emailStatus != null
                ? emailLogQueryService.getEmailLogsByStatus(emailStatus)
                : emailLogQueryService.getAllEmailLogs())
                .stream().map(EmailLogResponse::from).toList();

        List<EntityModel<EmailLogResponse>> models = logs.stream()
                .map(l -> EntityModel.of(l,
                        linkTo(methodOn(EmailLogQueryController.class).getEmailLog(l.emailLogId())).withSelfRel()))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(EmailLogQueryController.class).getEmailLogs(clientId, emailStatus)).withSelfRel()));
    }

    @GetMapping("/{emailLogId}")
    public ResponseEntity<EntityModel<EmailLogResponse>> getEmailLog(@PathVariable Long emailLogId) {
        EmailLogResponse response = emailLogQueryService.getEmailLog(emailLogId);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLogId)).withSelfRel(),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLogs(null, null)).withRel("email-logs")));
    }
}
