package com.team2.activity.query.controller;

import com.team2.activity.query.dto.ContactResponse;
import com.team2.activity.query.service.ContactQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<PagedModel<EntityModel<ContactResponse>>> getContacts(
            @Parameter(description = "검색 키워드 (이름/이메일)") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        // 컨택리스트는 작성자 개인의 인맥 자산. ADMIN 은 전체 조회, 나머지는 본인 writerId 만.
        // Buyer sync 는 같은 팀 sales 각각에 대해 별도 row 를 만들어주므로 팀 공유는 sync 단계에서 보장.
        Long writerId = isAdmin(jwt) ? null : Long.parseLong(jwt.getSubject());
        List<ContactResponse> contacts = contactQueryService.getContactsWithFilters(writerId, keyword, page, size)
                .stream().map(ContactResponse::from).toList();
        long totalElements = contactQueryService.countContactsWithFilters(writerId, keyword);
        List<EntityModel<ContactResponse>> models = contacts.stream().map(EntityModel::of).toList();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(size, page, totalElements);
        return ResponseEntity.ok(PagedModel.of(models, metadata));
    }

    private boolean isAdmin(Jwt jwt) {
        Object role = jwt.getClaim("role");
        return role != null && "ADMIN".equalsIgnoreCase(role.toString());
    }
}
