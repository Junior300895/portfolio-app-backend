package com.portfolio.service;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.model.Admin;
import com.portfolio.repository.AdminRepository;
import com.portfolio.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);

        Admin admin = adminRepository.findByUsername(request.getUsername()).orElseThrow();

        return AuthResponse.builder()
                .token(token)
                .username(admin.getUsername())
                .email(admin.getEmail())
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (!passwordEncoder.matches(oldPassword, admin.getPasswordHash())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }
}
