package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ContactCommandService;
import com.team2.activity.command.application.dto.ContactCreateRequest;
import com.team2.activity.command.application.dto.ContactUpdateRequest;
import com.team2.activity.command.domain.entity.Contact;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 연락처 쓰기 API를 제공하는 command controller다.
@RestController
// 생성자 주입용 생성자를 자동 생성한다.
@RequiredArgsConstructor
public class ContactCommandController {

    // 연락처 쓰기 로직을 서비스에 위임한다.
    private final ContactCommandService contactCommandService;

    // 특정 거래처에 새 연락처를 생성한다.
    @PostMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<Contact> createContact(
            // URL 경로에서 거래처 ID를 받는다.
            @PathVariable Long clientId,
            // 헤더에서 작성자 사용자 ID를 받는다.
            @RequestHeader("X-User-Id") Long userId,
            // 요청 본문을 검증한 뒤 DTO로 바인딩한다.
            @Valid @RequestBody ContactCreateRequest request) {
        // 요청 DTO와 경로/헤더 값을 합쳐 엔티티를 구성한다.
        Contact contact = Contact.builder()
                // 경로에서 받은 거래처 ID를 엔티티에 저장한다.
                .clientId(clientId)
                // 헤더에서 받은 사용자 ID를 작성자로 저장한다.
                .writerId(userId)
                // 요청의 이름을 엔티티에 저장한다.
                .contactName(request.contactName())
                // 요청의 직책을 엔티티에 저장한다.
                .contactPosition(request.contactPosition())
                // 요청의 이메일을 엔티티에 저장한다.
                .contactEmail(request.contactEmail())
                // 요청의 전화번호를 엔티티에 저장한다.
                .contactTel(request.contactTel())
                // 모든 필드 복사가 끝난 Contact 엔티티 생성을 마무리한다.
                .build();
        // 응답 상태 코드를 201 Created로 설정한다.
        return ResponseEntity.status(HttpStatus.CREATED)
                // 저장된 연락처 엔티티를 응답 본문으로 반환한다.
                .body(contactCommandService.createContact(contact));
    }

    // 기존 연락처의 수정 가능한 필드를 갱신한다.
    @PutMapping("/api/contacts/{contactId}")
    public ResponseEntity<Contact> updateContact(
            // URL 경로에서 수정 대상 연락처 ID를 받는다.
            @PathVariable Long contactId,
            // 요청 본문을 검증한 뒤 DTO로 바인딩한다.
            @Valid @RequestBody ContactUpdateRequest request) {
        // 수정할 각 필드를 서비스에 전달한다.
        Contact contact = contactCommandService.updateContact(
                // 수정 대상 연락처 ID를 전달한다.
                contactId,
                // 요청의 이름을 전달한다.
                request.contactName(),
                // 요청의 직책을 전달한다.
                request.contactPosition(),
                // 요청의 이메일을 전달한다.
                request.contactEmail(),
                // 요청의 전화번호를 전달한다.
                request.contactTel());
        // 수정 성공 응답 본문으로 갱신된 연락처를 반환한다.
        return ResponseEntity.ok(contact);
    }

    // 연락처 삭제 요청을 받아 대상 연락처를 제거한다.
    @DeleteMapping("/api/contacts/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long contactId) {
        // 삭제 처리는 서비스 계층에 위임한다.
        contactCommandService.deleteContact(contactId);
        // 응답 본문 없이 204 No Content를 반환한다.
        return ResponseEntity.noContent().build();
    }
}
