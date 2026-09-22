package com.staynest.api.config;

import com.staynest.api.entity.User;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import com.staynest.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures a SUPER_ADMIN account exists on startup ONLY when ADMIN_EMAIL and
 * ADMIN_PASSWORD environment variables are configured. Never uses a hardcoded
 * default password.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Admin bootstrap is OPT-IN via environment variables only.
        // Never create an account with a known/default password.
        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD not configured — skipping admin bootstrap.");
            return;
        }
        String email = adminEmail.toLowerCase().trim();
        userRepository.findByEmail(email).ifPresentOrElse(
            existingAdmin -> {
                log.debug("Admin account already exists: {}", email);
            },
            () -> {
                log.info("Bootstrapping SUPER_ADMIN account: {}", email);
                User admin = User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .firstName("Super")
                        .lastName("Admin")
                        .role(UserRole.SUPER_ADMIN)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .failedLoginAttempts(0)
                        .build();
                userRepository.save(admin);
                log.info("SUPER_ADMIN bootstrapped: {}", email);
            }
        );
    }
}
