package com.zest.productapi.service;

import com.zest.productapi.dto.AuthResponse;
import com.zest.productapi.dto.LoginRequest;
import com.zest.productapi.dto.RegisterRequest;
import com.zest.productapi.entity.RefreshToken;
import com.zest.productapi.entity.Role;
import com.zest.productapi.entity.User;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.repository.RefreshTokenRepository;
import com.zest.productapi.repository.UserRepository;
import com.zest.productapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // default role is USER unless ADMIN is asked for
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            user.setRole(Role.ROLE_ADMIN);
        } else {
            user.setRole(Role.ROLE_USER);
        }

        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // this throws if username/password is wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = createRefreshToken(user.getUsername());

        return new AuthResponse(accessToken, refreshToken);
    }

    // refresh token rotation: old token is deleted and a new one is issued
    @Transactional
    public AuthResponse refresh(String oldToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new BadCredentialsException("Refresh token expired, please login again");
        }

        User user = userRepository.findByUsername(stored.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // rotate: remove the used token and create a fresh one
        refreshTokenRepository.delete(stored);
        String newRefresh = createRefreshToken(user.getUsername());
        String newAccess = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(newAccess, newRefresh);
    }

    private String createRefreshToken(String username) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsername(username);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
