package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.SmsCouponParamDTO;
import org.example.mall_tiny01.mbg.model.SmsCoupon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SmsCouponService {

    void save(SmsCouponParamDTO couponParam);

    void delete(Long id);

    PageResult<SmsCoupon> list(String name, Integer type, Integer pageNum, Integer pageSize);

    List<SmsCoupon> listAll(String name, Integer type);

    void update(Long id, SmsCouponParamDTO couponParam);

    SmsCouponParamDTO getById(Long id);
}
