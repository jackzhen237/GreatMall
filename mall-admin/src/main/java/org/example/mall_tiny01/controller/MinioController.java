package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.service.MinioService;
import org.example.mall_tiny01.service.impl.MinioServiceimpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/minio")
@ApiOperation("文件处理")
public class MinioController {

    @Autowired
    private MinioService minioService;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        String url = minioService.uploadFile(file);
        return Result.success(url);
    }

    @PostMapping("/delete")
    @ApiOperation("文件删除")
    public Result delete(@RequestParam String objectName) {
        minioService.deleteFile(objectName);
        return Result.success("删除成功");
    }
}
