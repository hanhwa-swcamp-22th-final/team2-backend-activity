package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ContactCommandService;
import com.team2.activity.command.application.dto.ContactCreateRequest;
import com.team2.activity.command.application.dto.ContactUpdateRequest;
import com.team2.activity.query.controller.ContactQueryController;
import com.team2.activity.query.dto.ContactResponse;
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
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "연락처 Command", description = "연락처 생성/수정/삭제 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
public class ContactCommandController {

    private final ContactCommandService contactCommandService;

    @Operation(summary = "연락처 생성", description = "특정 거래처에 새로운 연락처를 생성한다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "연락처 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<EntityModel<ContactResponse>> createContact(
            @Parameter(description = "거래처 ID", required = true) @PathVariable("clientId") Long clientId,
            @Parameter(description = "요청 사용자 ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ContactCreateRequest request) {
        ContactResponse response = contactCommandService.createContact(clientId, userId, request);
        EntityModel<ContactResponse> model = EntityModel.of(response,
                linkTo(methodOn(ContactQueryController.class).getContactsByClientId(clientId)).withRel("contacts"));
        URI location = linkTo(methodOn(ContactQueryController.class).getContactsByClientId(clientId)).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "연락처 수정", description = "기존 연락처 정보를 수정한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연락처 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "연락처를 찾을 수 없음")
    })
    @PutMapping("/api/contacts/{contactId}")
    public ResponseEntity<EntityModel<ContactResponse>> updateContact(
            @Parameter(description = "연락처 ID", required = true) @PathVariable("contactId") Long contactId,
            @Valid @RequestBody ContactUpdateRequest request) {
        ContactResponse response = contactCommandService.updateContact(contactId, request);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(ContactQueryController.class).getContacts(null, null, 0, 20)).withRel("contacts")));
    }

    @Operation(summary = "연락처 삭제", description = "연락처를 삭제한다")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "연락처 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "연락처를 찾을 수 없음")
    })
    @DeleteMapping("/api/contacts/{contactId}")
    public ResponseEntity<Void> deleteContact(
            @Parameter(description = "연락처 ID", required = true) @PathVariable("contactId") Long contactId) {
        contactCommandService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }
}
