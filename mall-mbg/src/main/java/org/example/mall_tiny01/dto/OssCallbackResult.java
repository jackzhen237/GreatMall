package org.example.mall_tiny01.dto;

import lombok.Data;

@Data
public class OssCallbackResult {
    private String filename;
    private String size;
    private String mimeType;
    private String width;
    private String height;
}
