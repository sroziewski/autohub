package com.autohub.user_service.infrastructure.security;

import com.autohub.user_service.infrastructure.persistence.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userJpaRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("error.auth.invalid_credentials"));
    }
}
