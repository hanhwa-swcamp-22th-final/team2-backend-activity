package com.team2.activity.command.application.service;

import com.team2.activity.command.domain.entity.Contact;
import com.team2.activity.command.domain.repository.ContactRepository;
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

    // 새 연락처 엔티티를 저장한다.
    public Contact createContact(Contact contact) {
        // 전달받은 연락처 엔티티를 저장소에 저장한다.
        return contactRepository.save(contact);
    }

    // 기존 연락처를 찾아 수정 가능한 필드를 갱신한다.
    public Contact updateContact(Long contactId, String contactName, String contactPosition,
                                 String contactEmail, String contactTel) {
        // 수정 대상 연락처를 먼저 조회한다.
        Contact contact = findById(contactId);
        // 요청의 이름, 직책, 이메일, 전화번호를 엔티티에 반영한다.
        contact.update(contactName, contactPosition, contactEmail, contactTel);
        // 변경 감지 대상 엔티티를 그대로 반환한다.
        return contact;
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
