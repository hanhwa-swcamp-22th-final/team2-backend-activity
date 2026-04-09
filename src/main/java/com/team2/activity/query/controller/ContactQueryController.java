package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.query.dto.ContactResponse;
import com.team2.activity.query.service.ContactQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "연락처 Query", description = "연락처 조회 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ContactQueryController {

    private final ContactQueryService contactQueryService;

    @Operation(summary = "연락처 목록 조회", description = "필터 조건에 따라 연락처 목록을 페이징 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/api/contacts")
    public ResponseEntity<PagedResponse<ContactResponse>> getContacts(
            @Parameter(description = "거래처 ID (미입력 시 전체 조회)") @RequestParam(required = false) Long clientId,
            @Parameter(description = "검색 키워드 (이름/이메일)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {
        List<ContactResponse> contacts = contactQueryService.getContactsWithFilters(clientId, keyword, page, size)
                .stream().map(ContactResponse::from).toList();
        long totalElements = contactQueryService.countContactsWithFilters(clientId, keyword);
        return ResponseEntity.ok(PagedResponse.of(contacts, totalElements, page, size));
    }

    @Operation(summary = "거래처별 연락처 조회", description = "특정 거래처에 속한 연락처 목록을 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "거래처를 찾을 수 없음")
    })
    @GetMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<List<ContactResponse>> getContactsByClientId(
            @Parameter(description = "거래처 ID", required = true) @PathVariable Long clientId) {
        List<ContactResponse> contacts = contactQueryService.getContactsByClientId(clientId)
                .stream().map(ContactResponse::from).toList();
        return ResponseEntity.ok(contacts);
    }
}
