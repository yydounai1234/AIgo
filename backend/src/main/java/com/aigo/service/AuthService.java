package com.aigo.service;

import com.aigo.dto.auth.AuthResponse;
import com.aigo.dto.auth.LoginRequest;
import com.aigo.dto.auth.RegisterRequest;
import com.aigo.entity.User;
import com.aigo.exception.BusinessException;
import com.aigo.repository.UserRepository;
import com.aigo.security.JwtUtil;
import com.aigo.dto.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱已被注册");
        }
        
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .coinBalance(100)
            .build();
        
        user = userRepository.save(user);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        String token = jwtUtil.generateToken(user.getUsername(), claims);
        
        return AuthResponse.builder()
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .coinBalance(user.getCoinBalance())
                .createdAt(user.getCreatedAt().format(DATE_FORMATTER))
                .build())
            .token(token)
            .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        String token = jwtUtil.generateToken(user.getUsername(), claims);
        
        return AuthResponse.builder()
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .coinBalance(user.getCoinBalance())
                .build())
            .token(token)
            .build();
    }
}
