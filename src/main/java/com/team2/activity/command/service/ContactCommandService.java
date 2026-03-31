package com.team2.activity.command.service;

import com.team2.activity.command.repository.ContactRepository;
import com.team2.activity.entity.Contact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactCommandService {

    private final ContactRepository contactRepository;

    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public Contact updateContact(Long contactId, String contactName, String contactPosition,
                                 String contactEmail, String contactTel) {
        Contact contact = findById(contactId);
        contact.update(contactName, contactPosition, contactEmail, contactTel);
        return contact;
    }

    public void deleteContact(Long contactId) {
        Contact contact = findById(contactId);
        contactRepository.delete(contact);
    }

    private Contact findById(Long contactId) {
        return contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("연락처를 찾을 수 없습니다."));
    }
}
