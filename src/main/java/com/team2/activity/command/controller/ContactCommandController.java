package com.team2.activity.command.controller;

import com.team2.activity.command.service.ContactCommandService;
import com.team2.activity.dto.ContactCreateRequest;
import com.team2.activity.dto.ContactUpdateRequest;
import com.team2.activity.entity.Contact;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ContactCommandController {

    private final ContactCommandService contactCommandService;

    @PostMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<Contact> createContact(
            @PathVariable Long clientId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ContactCreateRequest request) {
        Contact contact = Contact.builder()
                .clientId(clientId)
                .writerId(userId)
                .contactName(request.contactName())
                .contactPosition(request.contactPosition())
                .contactEmail(request.contactEmail())
                .contactTel(request.contactTel())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactCommandService.createContact(contact));
    }

    @PutMapping("/api/contacts/{contactId}")
    public ResponseEntity<Contact> updateContact(
            @PathVariable Long contactId,
            @Valid @RequestBody ContactUpdateRequest request) {
        Contact contact = contactCommandService.updateContact(
                contactId,
                request.contactName(),
                request.contactPosition(),
                request.contactEmail(),
                request.contactTel());
        return ResponseEntity.ok(contact);
    }

    @DeleteMapping("/api/contacts/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long contactId) {
        contactCommandService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }
}
