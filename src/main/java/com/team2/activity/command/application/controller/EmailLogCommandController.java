package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.dto.EmailLogCreateRequest;
import com.team2.activity.command.application.dto.EmailLogInternalRequest;
import com.team2.activity.command.application.service.EmailLogCommandService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "이메일 로그 Command", description = "이메일 로그 생성 및 재발송 API")
@RestController
@RequestMapping("/api/email-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
public class EmailLogCommandController {

    private final EmailLogCommandService emailLogCommandService;

    @Operation(summary = "이메일 로그 생성", description = "새로운 이메일 로그를 생성한다. 실제 발송은 document 서비스가 담당한다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이메일 로그 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    public ResponseEntity<EntityModel<EmailLogResponse>> createEmailLog(
            @Parameter(description = "요청 사용자 ID", required = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody EmailLogCreateRequest request) {
        Long userId = jwt != null ? Long.parseLong(jwt.getSubject()) : 1L;
        EmailLog emailLog = emailLogCommandService.createEmailLog(request.toEntity(userId));
        EntityModel<EmailLogResponse> model = EntityModel.of(EmailLogResponse.from(emailLog),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLog.getEmailLogId())).withSelfRel(),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLogs(null, null, null, null, null, null, null, 0, 20)).withRel("email-logs"));
        URI location = linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLog.getEmailLogId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    // document 서비스가 메일 발송 후 결과를 activity 서비스에 기록하는 내부 전용 엔드포인트다.
    // X-Internal-Token 헤더로 InternalApiTokenFilter 가 별도 검증한다 (JWT 인증 주체 없음).
    // 따라서 클래스 레벨의 @PreAuthorize("hasAnyRole('ADMIN','SALES')") 를 우회해야 한다.
    @Operation(summary = "내부 이메일 로그 생성", description = "Documents 서비스에서 이메일 발송 후 이력을 저장한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 로그 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PreAuthorize("permitAll()")
    @PostMapping("/internal")
    public ResponseEntity<Void> createEmailLogInternal(
            @RequestBody EmailLogInternalRequest request) {
        emailLogCommandService.createEmailLogInternal(request);
        return ResponseEntity.ok().build();
    }

    // FAILED 상태의 이메일을 document 서비스를 통해 재전송한다.
    @Operation(summary = "이메일 재발송", description = "실패한 이메일을 재발송한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발송 성공"),
            @ApiResponse(responseCode = "404", description = "이메일 로그를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 발송된 이메일")
    })
    @PostMapping("/{emailLogId}/resend")
    public ResponseEntity<EntityModel<EmailLogResponse>> resend(
            @Parameter(description = "요청 사용자 ID", required = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable("emailLogId") Long emailLogId) {
        Long userId = jwt != null ? Long.parseLong(jwt.getSubject()) : 1L;
        EmailLogResponse response = EmailLogResponse.from(emailLogCommandService.resend(emailLogId, userId));
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(EmailLogQueryController.class).getEmailLog(emailLogId)).withSelfRel(),
                linkTo(methodOn(EmailLogQueryController.class).getEmailLogs(null, null, null, null, null, null, null, 0, 20)).withRel("email-logs")));
    }
}
