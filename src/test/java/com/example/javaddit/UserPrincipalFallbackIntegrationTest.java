package com.example.javaddit;

import com.example.javaddit.core.security.UserPrincipal;
import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserPrincipalFallbackIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void userPrincipal_fromUser_containsId_andUsername() {
        // arrange: уникальное имя, чтобы не нарушать unique index между запусками
        String uniqueUsername = "fallback_user_" + System.nanoTime();
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(uniqueUsername + "@example.com");
        user.setPasswordHash("noop");
        User savedUser = userRepository.save(user);

        // act
        UserPrincipal principal = UserPrincipal.fromUser(savedUser);

        // assert
        assertThat(principal.getId()).isNotNull();
        assertThat(principal.getId()).isEqualTo(savedUser.getId());
        assertThat(principal.getUsername()).isEqualTo(uniqueUsername);
    }
}
