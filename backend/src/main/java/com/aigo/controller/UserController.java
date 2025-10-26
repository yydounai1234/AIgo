package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import com.aigo.dto.user.BalanceResponse;
import com.aigo.dto.user.RechargeRequest;
import com.aigo.dto.user.RechargeResponse;
import com.aigo.security.JwtUtil;
import com.aigo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    private String getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractClaims(token).get("userId", String.class);
        }
        return null;
    }
    
    @GetMapping("/balance")
    public ApiResponse<BalanceResponse> getBalance(HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        BalanceResponse response = userService.getBalance(userId);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/recharge")
    public ApiResponse<RechargeResponse> rechargeCoins(@Valid @RequestBody RechargeRequest request,
                                                         HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        RechargeResponse response = userService.rechargeCoins(userId, request.getAmount());
        return ApiResponse.success(response);
    }
}
