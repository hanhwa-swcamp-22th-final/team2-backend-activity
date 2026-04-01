package com.team2.activity.query.service;

import com.team2.activity.entity.Contact;
import com.team2.activity.query.mapper.ContactQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 연락처 읽기 유스케이스를 담당하는 query service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 읽기 전용 트랜잭션으로 조회 성격을 명확히 한다.
@Transactional(readOnly = true)
public class ContactQueryService {

    // 연락처 조회용 MyBatis mapper다.
    private final ContactQueryMapper contactQueryMapper;

    // 연락처 ID로 단건을 조회하고 없으면 예외를 던진다.
    public Contact getContact(Long contactId) {
        // mapper를 호출해 contactId에 해당하는 연락처를 조회한다.
        Contact contact = contactQueryMapper.findById(contactId);
        // 조회 결과가 없으면 단건 조회 실패 예외를 던진다.
        if (contact == null) {
            throw new IllegalArgumentException("연락처를 찾을 수 없습니다.");
        }
        // 조회된 연락처 엔티티를 그대로 반환한다.
        return contact;
    }

    // 전체 연락처 목록을 조회한다.
    public List<Contact> getAllContacts() {
        // 전체 연락처 목록 조회를 mapper에 위임한다.
        return contactQueryMapper.findAll();
    }

    // 거래처 ID로 연락처 목록을 조회한다.
    public List<Contact> getContactsByClientId(Long clientId) {
        // 거래처 조건 목록 조회를 mapper에 위임한다.
        return contactQueryMapper.findAllByClientId(clientId);
    }
}
