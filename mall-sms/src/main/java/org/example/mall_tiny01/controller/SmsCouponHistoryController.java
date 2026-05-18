package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.CouponHistoryPageResult;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;
import org.example.mall_tiny01.service.SmsCouponHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/couponHistory")
@Api(tags = "优惠券领取记录管理")
public class SmsCouponHistoryController {

    @Autowired
    private SmsCouponHistoryService smsCouponHistoryService;

    @GetMapping("/list")
    @ApiOperation("根据优惠券id，使用状态，订单编号分页获取领取记录")
    public Result list(
            @RequestParam(value = "couponId", required = false) Long couponId,
            @RequestParam(value = "useStatus", required = false) Integer useStatus,
            @RequestParam(value = "orderSn", required = false) String orderSn,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        
        CouponHistoryPageResult param = new CouponHistoryPageResult();
        param.setCouponId(couponId);
        param.setUseStatus(useStatus);
        param.setOrderSn(orderSn);
        param.setPageNum(pageNum);
        param.setPageSize(pageSize);
        
        PageResult<SmsCouponHistory> result = smsCouponHistoryService.list(param);
        return Result.success(result);
    }
}