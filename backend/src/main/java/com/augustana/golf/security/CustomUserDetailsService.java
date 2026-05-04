package com.augustana.golf.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.augustana.golf.domain.model.User;
import com.augustana.golf.repository.UserRepository;

/**
 * Adapts the application's {@link User} entity to Spring Security's
 * {@link UserDetailsService} contract.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new CustomUserPrincipal(
                user.getUserId(),
                user.getUsername(),
                user.getPasswordHash()
        );
    }
}
