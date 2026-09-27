package com.usman.resourcebooking.util;

import com.usman.resourcebooking.model.Role;
import com.usman.resourcebooking.model.User;
import com.usman.resourcebooking.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed users already exist, skipping.");
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();

        User regularUser = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .build();

        userRepository.saveAll(List.of(admin, regularUser));

        log.info("Seeded default accounts: admin/admin123 (ADMIN), user1/user123 (USER)");
    }
}
