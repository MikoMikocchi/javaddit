package com.example.javaddit.core.security;

import com.example.javaddit.features.user.entity.User;
import com.example.javaddit.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.isBlank()) {
            throw new UsernameNotFoundException("Username or email must be provided");
        }

        String normalized = identifier.trim();
        User user = resolveUser(normalized);
        return UserPrincipal.fromUser(user);
    }

    private User resolveUser(String identifier) {
        return (identifier.contains("@"))
                ? userRepository.findByEmailIgnoreCase(identifier).orElseThrow(() ->
                        new UsernameNotFoundException("User not found"))
                : userRepository.findByUsernameIgnoreCase(identifier).orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }
}
