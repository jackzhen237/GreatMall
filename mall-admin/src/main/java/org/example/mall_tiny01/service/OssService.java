package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.OssPolicyResult;
import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    OssPolicyResult getPolicy();
    String uploadFile(MultipartFile file);
    void deleteFile(String objectName);
}
