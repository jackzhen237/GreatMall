package org.example.mall_tiny01.dto;

import lombok.Data;
import org.example.mall_tiny01.mbg.model.OmsCartItem;

import java.math.BigDecimal;

@Data
public class CartPromotionItem extends OmsCartItem {
    private String promotionMessage;
    private Integer realStock;
    private BigDecimal reduceAmount;
    private Integer integration;
    private Integer growth;
}