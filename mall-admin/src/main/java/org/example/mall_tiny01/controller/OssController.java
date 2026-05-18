package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.OssCallbackResult;
import org.example.mall_tiny01.dto.OssPolicyResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/aliyun/oss")
public class OssController {

    @Autowired
    private OssService ossService;

    @GetMapping("/policy")
    @ApiOperation("Oss上传签名生成")
    public Result<OssPolicyResult> policy() {
        OssPolicyResult result = ossService.getPolicy();
        return Result.success(result);
    }

    @PostMapping("/callback")
    @ApiOperation("Oss上传成功回调")
    public Result<OssCallbackResult> callback(@RequestBody OssCallbackResult ossCallbackResult) {
        return Result.success(ossCallbackResult);
    }

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        String url = ossService.uploadFile(file);
        return Result.success(url);
    }

    @PostMapping("/delete")
    @ApiOperation("文件删除")
    public Result<String> delete(@RequestParam String objectName) {
        ossService.deleteFile(objectName);
        return Result.success("删除成功");
    }
}
