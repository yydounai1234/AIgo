package com.aigo.controller;

import com.aigo.dto.user.BalanceResponse;
import com.aigo.security.JwtUtil;
import com.aigo.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private String token;
    private String userId;
    private Claims claims;

    @BeforeEach
    void setUp() {
        token = "Bearer test-token";
        userId = "user123";
        
        claims = new DefaultClaims();
        claims.put("userId", userId);

        when(jwtUtil.extractClaims(anyString())).thenReturn(claims);
    }

    @Test
    void testGetBalance() throws Exception {
        BalanceResponse balanceResponse = BalanceResponse.builder()
                .balance(150)
                .build();

        when(userService.getBalance(anyString())).thenReturn(balanceResponse);

        mockMvc.perform(get("/api/user/balance")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(150));
    }
}
