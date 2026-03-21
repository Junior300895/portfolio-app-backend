package com.portfolio.service;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.model.ContactMessage;
import com.portfolio.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.admin}")
    private String adminEmail;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Transactional
    public ContactMessageDTO saveMessage(ContactFormRequest req) {
        ContactMessage msg = ContactMessage.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .message(req.getMessage())
                .eventType(req.getEventType())
                .eventDateRequested(req.getEventDateRequested())
                .build();
        ContactMessage saved = contactMessageRepository.save(msg);
        sendNotificationEmail(saved);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ContactMessageDTO> getMessages(int page, int size) {
        Page<ContactMessage> result = contactMessageRepository.findAllByOrderBySentAtDesc(
                PageRequest.of(page, size));
        return PagedResponse.<ContactMessageDTO>builder()
                .content(result.getContent().stream().map(this::toDTO).toList())
                .page(result.getNumber()).size(result.getSize())
                .totalElements(result.getTotalElements()).totalPages(result.getTotalPages())
                .last(result.isLast()).build();
    }

    @Transactional
    public void markAsRead(Long id) {
        ContactMessage msg = contactMessageRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Message non trouvé: " + id));
        msg.setRead(true);
        contactMessageRepository.save(msg);
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return contactMessageRepository.countByReadFalse();
    }

    @Async
    protected void sendNotificationEmail(ContactMessage msg) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(adminEmail);
            email.setReplyTo(msg.getEmail());
            email.setSubject("[Portfolio] Nouveau message de " + msg.getName());
            email.setText(String.format(
                "Nouveau message de contact :\n\n" +
                "Nom : %s\nEmail : %s\nTéléphone : %s\n" +
                "Type d'événement : %s\nDate souhaitée : %s\n\n" +
                "Message :\n%s",
                msg.getName(), msg.getEmail(), msg.getPhone(),
                msg.getEventType(), msg.getEventDateRequested(), msg.getMessage()
            ));
            mailSender.send(email);
        } catch (Exception e) {
            log.warn("Could not send notification email: {}", e.getMessage());
        }
    }

    private ContactMessageDTO toDTO(ContactMessage m) {
        return ContactMessageDTO.builder()
                .id(m.getId()).name(m.getName()).email(m.getEmail()).phone(m.getPhone())
                .message(m.getMessage()).eventType(m.getEventType())
                .eventDateRequested(m.getEventDateRequested())
                .read(m.getRead()).sentAt(m.getSentAt())
                .build();
    }
}
