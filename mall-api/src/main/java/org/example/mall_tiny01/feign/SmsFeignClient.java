package org.example.mall_tiny01.feign;

import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.dto.SmsCouponParamDTO;
import org.example.mall_tiny01.mbg.model.SmsCoupon;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;
import org.example.mall_tiny01.mbg.model.SmsCouponProductCategoryRelation;
import org.example.mall_tiny01.mbg.model.SmsCouponProductRelation;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotion;
import org.example.mall_tiny01.mbg.model.SmsHomeAdvertise;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mall-sms")
public interface SmsFeignClient {

    @GetMapping("/coupon/{id}")
    Result<SmsCouponParamDTO> getCoupon(@PathVariable("id") Long id);

    @GetMapping("/coupon/listAll")
    Result<List<SmsCoupon>> listCoupons(@RequestParam(value = "name", required = false) String name,
                                        @RequestParam(value = "type", required = false) Integer type);

    @GetMapping("/coupon/productRelation/{couponId}")
    Result<List<SmsCouponProductRelation>> listCouponProductRelations(@PathVariable("couponId") Long couponId);

    @GetMapping("/coupon/categoryRelation/{couponId}")
    Result<List<SmsCouponProductCategoryRelation>> listCouponCategoryRelations(@PathVariable("couponId") Long couponId);

    @GetMapping("/coupon/history/list")
    Result<List<SmsCouponHistory>> listCouponHistory(@RequestParam("memberId") Long memberId,
                                                      @RequestParam("useStatus") Integer useStatus);

    @GetMapping("/flash/{id}")
    Result<SmsFlashPromotion> getFlashPromotion(@PathVariable("id") Long id);

    @GetMapping("/home/advertise/list")
    Result<List<SmsHomeAdvertise>> listHomeAdvertise(@RequestParam(value = "name", required = false) String name,
                                                      @RequestParam(value = "type", required = false) Integer type);

    @GetMapping("/coupon/simple/{id}")
    Result<SmsCoupon> getCouponSimple(@PathVariable("id") Long id);

    @PostMapping("/coupon/history/add")
    Result addCouponHistory(@RequestBody SmsCouponHistory history);
}
