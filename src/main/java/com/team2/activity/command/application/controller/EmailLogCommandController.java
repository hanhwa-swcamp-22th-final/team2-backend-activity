package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.EmailLogCommandService;
import com.team2.activity.command.application.dto.EmailLogCreateRequest;
import com.team2.activity.command.application.dto.EmailLogInternalRequest;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.query.controller.EmailLogQueryController;
import com.team2.activity.query.dto.EmailLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "이메일 로그 Command", description = "이메일 로그 생성 및 재발송 API")
@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
public class EmailLogCommandController {

    private final EmailLogCommandService emailLogCommandService;

    @Operation(summary = "이메일 로그 생성", description = "새로운 이메일 로그를 생성하고 발송을 시도한다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이메일 로그 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    public ResponseEntity<EntityModel<EmailLogResponse>> createEmailLog(
            @Parameter(description = "요청 사용자 ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody EmailLogCreateRequest request) {
        EmailLog emailLog = emailLogCommandService.createEmailLog(request.toEntity(userId));
        emailLogCommandService.attemptSend(emailLog);
        EntityModel<EmailLogResponse> model = EntityModel.of(EmailLogResponse.from(emailLog),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLog.getEmailLogId())).withSelfRel(),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLogs(null, null, null, null, null, null, null, 0, 20)).withRel("email-logs"));
        URI location = linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLog.getEmailLogId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PostMapping("/internal")
    @Operation(summary = "내부 이메일 로그 생성", description = "Documents 서비스에서 이메일 발송 후 이력을 저장합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이메일 로그 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    public ResponseEntity<Void> createEmailLogInternal(@Valid @RequestBody EmailLogInternalRequest request) {
        emailLogCommandService.createEmailLogFromInternal(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "이메일 재발송", description = "실패한 이메일을 재발송한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발송 성공"),
            @ApiResponse(responseCode = "404", description = "이메일 로그를 찾을 수 없음")
    })
    @PostMapping("/{emailLogId}/resend")
    public ResponseEntity<EntityModel<EmailLogResponse>> resend(
            @Parameter(description = "이메일 로그 ID", required = true) @PathVariable Long emailLogId) {
        EmailLogResponse response = EmailLogResponse.from(emailLogCommandService.resend(emailLogId));
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLogId)).withSelfRel(),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLogs(null, null, null, null, null, null, null, 0, 20)).withRel("email-logs")));
    }
}
