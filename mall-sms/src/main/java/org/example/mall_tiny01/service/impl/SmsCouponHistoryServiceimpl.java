package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.CouponHistoryPageResult;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.SmsCouponHistoryMapper;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;
import org.example.mall_tiny01.service.SmsCouponHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsCouponHistoryServiceimpl implements SmsCouponHistoryService {
    @Autowired
    private SmsCouponHistoryMapper smsCouponHistoryMapper;
    @Override
    public PageResult<SmsCouponHistory> list(CouponHistoryPageResult param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize());

        List<SmsCouponHistory> list = smsCouponHistoryMapper.list(
                param.getCouponId(),
                param.getUseStatus(),
                param.getOrderSn()
        );

        PageInfo<SmsCouponHistory> pageInfo = new PageInfo<>(list);

        PageResult<SmsCouponHistory> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());

        return result;
    }
}
