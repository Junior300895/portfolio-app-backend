package com.portfolio.controller;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessageDTO>> sendMessage(
            @Valid @RequestBody ContactFormRequest request) {
        ContactMessageDTO saved = contactService.saveMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message envoyé avec succès !", saved));
    }
}
