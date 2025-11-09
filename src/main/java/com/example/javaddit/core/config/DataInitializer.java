package com.example.javaddit.core.config;

import com.example.javaddit.features.user.entity.Role;
import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.user.repository.RoleRepository;
import com.example.javaddit.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role userRole = ensureDefaultRole();

        if (userRepository.count() == 0) {
            User defaultUser = new User();
            defaultUser.setUsername("default_user");
            defaultUser.setEmail("default@javaddit.com");
            defaultUser.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
            defaultUser.setDisplayName("Default User");
            defaultUser.setAbout("Default user for testing");
            defaultUser.setIsDeleted(false);
            defaultUser.getRoles().add(userRole);

            userRepository.save(defaultUser);
            log.info("Created default user: {}", defaultUser.getUsername());
        }
    }

    private Role ensureDefaultRole() {
        return roleRepository.findById("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setCode("USER");
                    role.setDisplayName("User");
                    role.setDescription("Default application user role");
                    Role saved = roleRepository.save(role);
                    log.info("Created default role: {}", saved.getCode());
                    return saved;
                });
    }
}
