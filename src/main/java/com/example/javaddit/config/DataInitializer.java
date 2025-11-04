package com.example.javaddit.config;

import com.example.javaddit.entity.User;
import com.example.javaddit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        // Create a default user if none exists
        if (userRepository.count() == 0) {
            User defaultUser = new User();
            defaultUser.setUsername("default_user");
            defaultUser.setEmail("default@javaddit.com");
            defaultUser.setPasswordHash("$2a$10$dummyhashfortemporarytesting");
            defaultUser.setDisplayName("Default User");
            defaultUser.setAbout("Default user for testing");
            defaultUser.setIsDeleted(false);

            userRepository.save(defaultUser);
            log.info("Created default user: {}", defaultUser.getUsername());
        }
    }
}
