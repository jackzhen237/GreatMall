package org.example.mall_tiny01.dto;

import lombok.Data;
import org.example.mall_tiny01.mbg.model.SmsCoupon;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;
import org.example.mall_tiny01.mbg.model.SmsCouponProductCategoryRelation;
import org.example.mall_tiny01.mbg.model.SmsCouponProductRelation;

import java.util.Date;
import java.util.List;

@Data
public class SmsCouponHistoryDetail extends SmsCouponHistory {
    private SmsCoupon coupon;
    private List<SmsCouponProductRelation> productRelationList;
    private List<SmsCouponProductCategoryRelation> categoryRelationList;
}