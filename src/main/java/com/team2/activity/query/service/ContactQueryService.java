package com.team2.activity.query.service;

import com.team2.activity.entity.Contact;
import com.team2.activity.query.mapper.ContactQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactQueryService {

    private final ContactQueryMapper contactQueryMapper;

    public Contact getContact(Long contactId) {
        Contact contact = contactQueryMapper.findById(contactId);
        if (contact == null) {
            throw new IllegalArgumentException("연락처를 찾을 수 없습니다.");
        }
        return contact;
    }

    public List<Contact> getAllContacts() {
        return contactQueryMapper.findAll();
    }

    public List<Contact> getContactsByClientId(Long clientId) {
        return contactQueryMapper.findAllByClientId(clientId);
    }
}
