package org.example.mall_tiny01.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SmsFlashPromotionProduct {
    private Long id;
    private Long flashPromotionId;
    private Long flashPromotionSessionId;
    private Long productId;
    private String productName;
    private String productPic;
    private BigDecimal flashPromotionPrice;
    private Integer sort;
}