package org.example.mall_tiny01.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalcAmount {
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal promotionAmount;
    private BigDecimal payAmount;
}