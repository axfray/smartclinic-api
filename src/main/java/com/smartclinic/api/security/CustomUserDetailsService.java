package com.smartclinic.api.security;

import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Usuario no encontrado.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (user.getPasswordHash() == null) {
            throw new UsernameNotFoundException("El usuario no tiene una contraseña configurada.");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password(user.getPasswordHash())
                .authorities(user.getRole() != null ? user.getRole().name() : "ROLE_PATIENT")
                .build();
    }
}
