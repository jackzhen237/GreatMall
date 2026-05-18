package org.example.mall_tiny01.dto;

import lombok.Data;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotion;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotionSession;

import java.util.List;

@Data
public class HomeFlashPromotion {
    private Long id;
    private String title;
    private Integer status;
    private String startDate;
    private String endDate;
    private List<SmsFlashPromotionSession> sessionList;
    private List<SmsFlashPromotionProduct> productList;
}