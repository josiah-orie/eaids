package com.feadev.eaids.security;

import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("Invalid user ID format: " + userId);
        }
        return loadUserById(id);
    }

    public CustomUserDetails loadUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        return new CustomUserDetails(user);
    }
}
