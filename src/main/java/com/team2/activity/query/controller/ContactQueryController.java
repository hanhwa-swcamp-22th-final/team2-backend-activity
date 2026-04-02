package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.query.dto.ContactResponse;
import com.team2.activity.query.service.ContactQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 연락처 읽기 API를 제공하는 query controller다.
@RestController
// 생성자 주입용 생성자를 자동 생성한다.
@RequiredArgsConstructor
public class ContactQueryController {

    // 연락처 조회 로직을 서비스에 위임한다.
    private final ContactQueryService contactQueryService;

    // 조건에 맞는 연락처 목록을 페이징 형태로 조회한다.
    @GetMapping("/api/contacts")
    public ResponseEntity<PagedResponse<ContactResponse>> getContacts(
            // 필요하면 거래처 ID 조건으로 조회를 제한한다.
            @RequestParam(required = false) Long clientId) {
        // 파라미터 유무에 따라 전체 조회와 거래처별 조회를 분기한다.
        List<ContactResponse> contacts = (clientId != null
                // clientId가 있으면 거래처별 연락처 목록을 조회한다.
                ? contactQueryService.getContactsByClientId(clientId)
                // clientId가 없으면 전체 연락처 목록을 조회한다.
                : contactQueryService.getAllContacts())
                // 조회된 Contact 엔티티 목록을 DTO 목록으로 변환한다.
                .stream().map(ContactResponse::from).toList();
        // 조회된 연락처 DTO 목록을 단일 페이지 응답 구조로 감싸 200 OK로 반환한다.
        return ResponseEntity.ok(PagedResponse.of(contacts));
    }

    // 거래처 ID로 연락처 목록을 직접 조회하는 별도 엔드포인트다.
    @GetMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<List<ContactResponse>> getContactsByClientId(@PathVariable Long clientId) {
        // 거래처 조건 연락처 목록을 조회해 DTO로 변환한 뒤 200 OK로 반환한다.
        List<ContactResponse> contacts = contactQueryService.getContactsByClientId(clientId)
                // 조회된 Contact 엔티티 각각을 ContactResponse DTO로 변환한다.
                .stream().map(ContactResponse::from).toList();
        // 변환된 DTO 목록을 응답 본문으로 반환한다.
        return ResponseEntity.ok(contacts);
    }
}
