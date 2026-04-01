package com.team2.activity.query.controller;

import com.team2.activity.entity.Contact;
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

    // 조건에 맞는 연락처 목록을 조회한다.
    @GetMapping("/api/contacts")
    public ResponseEntity<List<Contact>> getContacts(
            // 필요하면 거래처 ID 조건으로 조회를 제한한다.
            @RequestParam(required = false) Long clientId) {
        // 파라미터 유무에 따라 전체 조회와 거래처별 조회를 분기한다.
        List<Contact> contacts = clientId != null
                // clientId가 있으면 거래처별 연락처 목록을 조회한다.
                ? contactQueryService.getContactsByClientId(clientId)
                // clientId가 없으면 전체 연락처 목록을 조회한다.
                : contactQueryService.getAllContacts();
        // 조회된 연락처 목록을 200 OK로 반환한다.
        return ResponseEntity.ok(contacts);
    }

    // 거래처 ID로 연락처 목록을 직접 조회하는 별도 엔드포인트다.
    @GetMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<List<Contact>> getContactsByClientId(@PathVariable Long clientId) {
        // 거래처 조건 연락처 목록을 조회해 200 OK로 반환한다.
        return ResponseEntity.ok(contactQueryService.getContactsByClientId(clientId));
    }
}
