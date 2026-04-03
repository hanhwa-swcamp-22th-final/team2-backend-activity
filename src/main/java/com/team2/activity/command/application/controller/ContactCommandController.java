package com.team2.activity.command.application.controller;

import com.team2.activity.command.application.service.ContactCommandService;
import com.team2.activity.command.application.dto.ContactCreateRequest;
import com.team2.activity.command.application.dto.ContactUpdateRequest;
import com.team2.activity.command.domain.entity.Contact;
import com.team2.activity.query.controller.ContactQueryController;
import com.team2.activity.query.dto.ContactResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
public class ContactCommandController {

    private final ContactCommandService contactCommandService;

    @PostMapping("/api/clients/{clientId}/contacts")
    public ResponseEntity<EntityModel<ContactResponse>> createContact(
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
        Contact saved = contactCommandService.createContact(contact);
        EntityModel<ContactResponse> model = EntityModel.of(ContactResponse.from(saved),
                linkTo(methodOn(ContactQueryController.class).getContactsByClientId(clientId)).withRel("contacts"));
        URI location = linkTo(methodOn(ContactQueryController.class).getContactsByClientId(clientId)).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/api/contacts/{contactId}")
    public ResponseEntity<EntityModel<ContactResponse>> updateContact(
            @PathVariable Long contactId,
            @Valid @RequestBody ContactUpdateRequest request) {
        Contact contact = contactCommandService.updateContact(
                contactId,
                request.contactName(),
                request.contactPosition(),
                request.contactEmail(),
                request.contactTel());
        return ResponseEntity.ok(EntityModel.of(ContactResponse.from(contact),
                linkTo(methodOn(ContactQueryController.class).getContacts(null)).withRel("contacts")));
    }

    @DeleteMapping("/api/contacts/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long contactId) {
        contactCommandService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }
}
