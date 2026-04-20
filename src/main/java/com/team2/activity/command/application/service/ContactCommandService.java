package com.team2.activity.command.application.service;

import com.team2.activity.command.application.dto.ContactCreateRequest;
import com.team2.activity.command.application.dto.ContactInternalRequest;
import com.team2.activity.command.application.dto.ContactUpdateRequest;
import com.team2.activity.command.domain.entity.Contact;
import com.team2.activity.command.domain.repository.ContactRepository;
import com.team2.activity.query.dto.ContactResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 연락처 쓰기 유스케이스를 담당하는 command service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 쓰기 작업이 하나의 트랜잭션으로 처리되도록 보장한다.
@Transactional
public class ContactCommandService {

    // 연락처 저장소 접근을 담당한다.
    private final ContactRepository contactRepository;

    // 새 연락처를 생성하고 응답 DTO를 반환한다. 컨택리스트는 거래처 무관 개인 주소록.
    public ContactResponse createContact(Long userId, ContactCreateRequest request) {
        Contact contact = contactRepository.save(request.toEntity(userId));
        return ContactResponse.from(contact);
    }

    // Master 서비스의 Buyer 생성 이벤트에서 호출되는 내부 동기화 메서드.
    // 같은 팀 sales 각각에 대해 buyer 정보를 개인 컨택으로 자동 추가.
    // idempotency: 같은 (writerId, email) 이미 존재하면 skip. sync 가 재실행돼도
    // 중복 row 가 쌓이지 않도록 (Issue #11 — 동일 컨택이 2배로 노출되던 문제).
    // email 이 비어 있는 경우엔 이름만으로는 유니크 판정을 못 하니 그대로 저장.
    public void createContactInternal(ContactInternalRequest request) {
        String email = request.contactEmail();
        if (email != null && !email.isBlank()
                && contactRepository.existsByWriterIdAndContactEmail(request.writerId(), email)) {
            return;
        }
        Contact contact = Contact.builder()
                .writerId(request.writerId())
                .contactName(request.contactName())
                .contactPosition(request.contactPosition())
                .contactEmail(request.contactEmail())
                .contactTel(request.contactTel())
                .build();
        contactRepository.save(contact);
    }

    // 기존 연락처를 찾아 수정 가능한 필드를 갱신한다.
    public ContactResponse updateContact(Long contactId, ContactUpdateRequest request) {
        Contact contact = findById(contactId);
        contact.update(request.contactName(), request.contactPosition(),
                request.contactEmail(), request.contactTel());
        return ContactResponse.from(contact);
    }

    // 연락처를 조회한 뒤 삭제한다.
    public void deleteContact(Long contactId) {
        // 삭제 대상 연락처를 먼저 조회한다.
        Contact contact = findById(contactId);
        // 조회한 연락처를 저장소에서 삭제한다.
        contactRepository.delete(contact);
    }

    // ID로 연락처를 조회하고 없으면 예외를 던진다.
    private Contact findById(Long contactId) {
        return contactRepository.findById(contactId)
                // 조회 결과가 없으면 연락처 없음 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("연락처를 찾을 수 없습니다."));
    }
}
