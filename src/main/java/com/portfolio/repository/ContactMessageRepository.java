package com.portfolio.repository;

import com.portfolio.model.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    Page<ContactMessage> findAllByOrderBySentAtDesc(Pageable pageable);
    long countByReadFalse();  // Hibernate maps Java field 'read' → column 'is_read'
}
