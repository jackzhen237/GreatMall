package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.SmsCouponHistoryDetail;
import org.example.mall_tiny01.mbg.model.SmsCoupon;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;

import java.util.List;

public interface MemberCouponService {
    void add(Long memberId, Long couponId);
    
    List<SmsCoupon> list(Long memberId, Integer useStatus);
    
    List<SmsCouponHistory> listHistory(Long memberId, Integer useStatus);
    
    List<SmsCouponHistoryDetail> listCart(Long memberId, Integer type);
    
    List<SmsCoupon> listByProduct(Long memberId, Long productId);
}
