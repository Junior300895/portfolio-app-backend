package com.portfolio.config;

import com.portfolio.model.Admin;
import com.portfolio.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        if (adminRepository.existsByUsername("admin")) {
            log.info("Admin account already exists — skipping seed");
            return;
        }

        Admin admin = Admin.builder()
                .username("admin")
                .email("admin@portfolio.com")
                .passwordHash(passwordEncoder.encode("Admin@2025"))
                .build();

        adminRepository.save(admin);
        log.info("========================================");
        log.info("  Default admin account created:");
        log.info("  Username : admin");
        log.info("  Password : Admin@2025");
        log.info("  >>> Change this password immediately! <<<");
        log.info("========================================");
    }
}
