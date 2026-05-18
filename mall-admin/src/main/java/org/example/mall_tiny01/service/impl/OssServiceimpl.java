package org.example.mall_tiny01.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.example.mall_tiny01.config.OssConfig;
import org.example.mall_tiny01.dto.OssPolicyResult;
import org.example.mall_tiny01.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class OssServiceimpl implements OssService {
    @Autowired
    private OssConfig ossConfig;

    @Override
    public OssPolicyResult getPolicy() {
        OssPolicyResult result = new OssPolicyResult();

        // 1. 生成上传目录：格式如 images/20240415/
        String dir = ossConfig.getDirPrefix() + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/";
        result.setDir(dir);

        // 2. 构造 Callback（上传成功后阿里云会回调这个地址）
        String callback = "{\"callbackUrl\":\"" + ossConfig.getCallbackHost() + "/aliyun/oss/callback\"," +
                "\"callbackBody\":\"filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}\"}";
        result.setCallback(callback);

        // 3. 构造 Policy（包含过期时间、文件大小限制等）
        long expireTime = 300; // 5分钟后过期
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
        Date expiration = new Date(expireEndTime);
        String policy = "{\"expiration\":\"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(expiration) + "\"," +
                "\"conditions\":[[\"content-length-range\",0,1048576000]]}"; // 最大 1GB

        // 4. 对 Policy 进行 Base64 编码
        String base64Policy = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));
        result.setPolicy(base64Policy);

        // 5. 使用 AccessKeySecret 对 Policy 进行签名
        String signature = hmacSHA1(ossConfig.getAccessKeySecret(), base64Policy);
        result.setSignature(signature);

        // 6. 填充其他字段
        result.setAccessKeyId(ossConfig.getAccessKeyId());
        result.setHost("https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint());

        return result;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String dir = ossConfig.getDirPrefix() + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/";
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = dir + UUID.randomUUID().toString().replace("-", "") + extension;

        OSS ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        } finally {
            ossClient.shutdown();
        }
        return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
    }

    @Override
    public void deleteFile(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
        try {
            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    // HMAC-SHA1 签名算法
    private String hmacSHA1(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signData);
        } catch (Exception e) {
            throw new RuntimeException("签名失败", e);
        }
    }
}
