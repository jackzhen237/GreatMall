package org.example.mall_tiny01.dto;

import lombok.Data;

@Data
public class OssPolicyResult {
    private String accessKeyId;
    private String callback;
    private String dir;
    private String host;
    private String policy;
    private String signature;
}
