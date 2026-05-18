package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.CouponHistoryPageResult;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;
import org.springframework.stereotype.Service;

@Service
public interface SmsCouponHistoryService {
    PageResult<SmsCouponHistory> list(CouponHistoryPageResult param);
}
