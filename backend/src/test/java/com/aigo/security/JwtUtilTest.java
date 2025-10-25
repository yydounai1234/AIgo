package com.aigo.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private String testUsername;
    private Map<String, Object> testClaims;

    @BeforeEach
    void setUp() {
        testUsername = "testuser";
        testClaims = new HashMap<>();
        testClaims.put("userId", "123");
        testClaims.put("email", "test@example.com");
    }

    @Test
    void testGenerateTokenWithClaims() {
        String token = jwtUtil.generateToken(testUsername, testClaims);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateTokenWithoutClaims() {
        String token = jwtUtil.generateToken(testUsername);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken(testUsername, testClaims);
        String extractedUsername = jwtUtil.extractUsername(token);
        
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void testExtractClaims() {
        String token = jwtUtil.generateToken(testUsername, testClaims);
        Claims claims = jwtUtil.extractClaims(token);
        
        assertNotNull(claims);
        assertEquals("123", claims.get("userId", String.class));
        assertEquals("test@example.com", claims.get("email", String.class));
        assertEquals(testUsername, claims.getSubject());
    }

    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateToken(testUsername, testClaims);
        
        assertNotNull(jwtUtil.extractExpiration(token));
        assertTrue(jwtUtil.extractExpiration(token).getTime() > System.currentTimeMillis());
    }

    @Test
    void testIsTokenNotExpired() {
        String token = jwtUtil.generateToken(testUsername, testClaims);
        
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken(testUsername, testClaims);
        
        assertTrue(jwtUtil.validateToken(token, testUsername));
        assertFalse(jwtUtil.validateToken(token, "wronguser"));
    }
}
