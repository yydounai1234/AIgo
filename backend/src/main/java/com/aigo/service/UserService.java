package com.aigo.service;

import com.aigo.dto.ErrorCode;
import com.aigo.dto.user.BalanceResponse;
import com.aigo.dto.user.RechargeResponse;
import com.aigo.dto.user.UploadAvatarResponse;
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
    private final QiniuStorageService qiniuStorageService;
    
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        
        return BalanceResponse.builder()
                .balance(user.getCoinBalance())
                .build();
    }
    
    @Transactional
    public RechargeResponse rechargeCoins(String userId, Integer amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "充值金额必须大于0");
        }
        
        if (amount > 1000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单次充值金额不能超过1000金币");
        }
        
        user.setCoinBalance(user.getCoinBalance() + amount);
        userRepository.save(user);
        
        return RechargeResponse.builder()
                .rechargeAmount(amount)
                .newBalance(user.getCoinBalance())
                .build();
    }
    
    @Transactional
    public UploadAvatarResponse uploadAvatar(String userId, String avatarData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        
        if (avatarData == null || avatarData.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "头像数据不能为空");
        }
        
        String avatarUrl = qiniuStorageService.uploadBase64Image(avatarData, "avatar");
        
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        
        return UploadAvatarResponse.builder()
                .avatarUrl(avatarUrl)
                .message("头像上传成功")
                .build();
    }
}
