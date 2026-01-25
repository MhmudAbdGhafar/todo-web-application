package org.example.user_web_service.services;

import lombok.RequiredArgsConstructor;
import org.example.user_web_service.entities.User;
import org.example.user_web_service.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) {

        return userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException
                        ("User not found")
                );
    }
}