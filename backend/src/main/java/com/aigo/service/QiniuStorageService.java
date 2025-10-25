package com.aigo.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
public class QiniuStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(QiniuStorageService.class);
    
    @Value("${qiniu.storage.access.key}")
    private String accessKey;
    
    @Value("${qiniu.storage.secret.key}")
    private String secretKey;
    
    @Value("${qiniu.storage.bucket.name}")
    private String bucketName;
    
    @Value("${qiniu.storage.domain}")
    private String domain;
    
    private UploadManager uploadManager;
    private Auth auth;
    
    private void initializeIfNeeded() {
        if (uploadManager == null) {
            Configuration cfg = new Configuration(Region.autoRegion());
            uploadManager = new UploadManager(cfg);
            auth = Auth.create(accessKey, secretKey);
        }
    }
    
    public String uploadBase64Image(String base64Data, String filePrefix) {
        if ("demo-key".equals(accessKey)) {
            logger.info("[QiniuStorageService] Using demo mode, returning placeholder URL");
            return "https://via.placeholder.com/1024x1024.png?text=" + filePrefix;
        }
        
        initializeIfNeeded();
        
        try {
            byte[] imageBytes = decodeBase64Image(base64Data);
            
            String fileName = generateFileName(filePrefix);
            
            String uploadToken = auth.uploadToken(bucketName);
            
            Response response = uploadManager.put(imageBytes, fileName, uploadToken);
            
            if (response.isOK()) {
                String publicUrl = buildPublicUrl(fileName);
                logger.info("[QiniuStorageService] Successfully uploaded image: {}", publicUrl);
                return publicUrl;
            } else {
                throw new RuntimeException("Upload failed with status: " + response.statusCode);
            }
            
        } catch (QiniuException e) {
            logger.error("[QiniuStorageService] Failed to upload image to Qiniu", e);
            throw new RuntimeException("上传图片到七牛云失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("[QiniuStorageService] Unexpected error during upload", e);
            throw new RuntimeException("上传图片失败: " + e.getMessage(), e);
        }
    }
    
    private byte[] decodeBase64Image(String base64Data) {
        String base64Content = base64Data;
        if (base64Data.contains(",")) {
            base64Content = base64Data.split(",")[1];
        }
        return Base64.getDecoder().decode(base64Content);
    }
    
    private String generateFileName(String prefix) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("%s_%s_%s.png", prefix, timestamp, uuid);
    }
    
    private String buildPublicUrl(String fileName) {
        String domainUrl = domain;
        if (!domainUrl.startsWith("http://") && !domainUrl.startsWith("https://")) {
            domainUrl = "https://" + domainUrl;
        }
        if (!domainUrl.endsWith("/")) {
            domainUrl += "/";
        }
        return domainUrl + fileName;
    }
}
