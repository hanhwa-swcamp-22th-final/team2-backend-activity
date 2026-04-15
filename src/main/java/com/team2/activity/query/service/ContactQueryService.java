package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.Contact;
import com.team2.activity.query.mapper.ContactQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 연락처 읽기 유스케이스를 담당하는 query service다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactQueryService {

    private final ContactQueryMapper contactQueryMapper;

    public Contact getContact(Long contactId) {
        Contact contact = contactQueryMapper.findContactById(contactId);
        if (contact == null) {
            throw new IllegalArgumentException("연락처를 찾을 수 없습니다.");
        }
        return contact;
    }

    public List<Contact> getAllContacts() {
        return contactQueryMapper.findAllContacts();
    }

    public List<Contact> getContactsByClientId(Long clientId) {
        return contactQueryMapper.findAllContactsByClientId(clientId);
    }

    public List<Contact> getContactsWithFilters(Long clientId, Long writerId, String keyword, int page, int size) {
        int offset = page * size;
        return contactQueryMapper.findContactsWithFilters(clientId, writerId, keyword, size, offset);
    }

    public long countContactsWithFilters(Long clientId, Long writerId, String keyword) {
        return contactQueryMapper.countContactsWithFilters(clientId, writerId, keyword);
    }
}
