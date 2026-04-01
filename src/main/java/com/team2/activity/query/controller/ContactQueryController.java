package com.team2.activity.query.controller;

import com.team2.activity.entity.Contact;
import com.team2.activity.query.service.ContactQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ContactQueryController {

    private final ContactQueryService contactQueryService;

    @GetMapping("/api/contacts")
    public ResponseEntity<List<Contact>> getContacts(
            @RequestParam(required = false) Long clientId) {
        List<Contact> contacts = clientId != null
                ? contactQueryService.getContactsByClientId(clientId)
                : contactQueryService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<List<Contact>> getContactsByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(contactQueryService.getContactsByClientId(clientId));
    }
}
