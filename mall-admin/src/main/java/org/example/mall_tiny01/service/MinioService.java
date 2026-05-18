package org.example.mall_tiny01.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    String uploadFile(MultipartFile file);

    void deleteFile(String objectName);
}