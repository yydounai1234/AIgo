package com.aigo.service;

import com.aigo.dto.ErrorCode;
import com.aigo.dto.user.BalanceResponse;
import com.aigo.entity.User;
import com.aigo.exception.BusinessException;
import com.aigo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        
        return BalanceResponse.builder()
                .balance(user.getCoinBalance())
                .build();
    }
}
