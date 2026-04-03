package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.query.dto.ContactResponse;
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
    public ResponseEntity<PagedResponse<ContactResponse>> getContacts(
            @RequestParam(required = false) Long clientId) {
        List<ContactResponse> contacts = (clientId != null
                ? contactQueryService.getContactsByClientId(clientId)
                : contactQueryService.getAllContacts())
                .stream().map(ContactResponse::from).toList();
        return ResponseEntity.ok(PagedResponse.of(contacts));
    }

    @GetMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<List<ContactResponse>> getContactsByClientId(@PathVariable Long clientId) {
        List<ContactResponse> contacts = contactQueryService.getContactsByClientId(clientId)
                .stream().map(ContactResponse::from).toList();
        return ResponseEntity.ok(contacts);
    }
}
