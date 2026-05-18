package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.dto.SmsCouponHistoryDetail;
import org.example.mall_tiny01.mbg.mapper.UmsMemberMapper;
import org.example.mall_tiny01.mbg.model.SmsCoupon;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.service.MemberCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "MemberCouponController", description = "会员优惠券管理")
@RestController
@RequestMapping("/member/coupon")
public class MemberCouponController {
    
    @Autowired
    private MemberCouponService memberCouponService;
    
    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @PostMapping("/add/{couponId}")
    @ApiOperation("领取指定优惠券")
    public Result add(@PathVariable Long couponId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            memberCouponService.add(member.getId(), couponId);
            return Result.success("领取成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation("获取会员优惠券列表")
    public Result<List<SmsCoupon>> list(
            @RequestParam(value = "useStatus", required = false) Integer useStatus) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            List<SmsCoupon> couponList = memberCouponService.list(member.getId(), useStatus);
            return Result.success(couponList);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/listHistory")
    @ApiOperation("获取会员优惠券历史列表")
    public Result<List<SmsCouponHistory>> listHistory(
            @RequestParam(value = "useStatus", required = false) Integer useStatus) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            List<SmsCouponHistory> historyList = memberCouponService.listHistory(member.getId(), useStatus);
            return Result.success(historyList);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list/cart/{type}")
    @ApiOperation("获取登录会员购物车的相关优惠券")
    public Result<List<SmsCouponHistoryDetail>> listCart(
            @PathVariable Integer type) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            List<SmsCouponHistoryDetail> detailList = memberCouponService.listCart(member.getId(), type);
            return Result.success(detailList);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/listByProduct/{productId}")
    @ApiOperation("获取当前商品相关优惠券")
    public Result<List<SmsCoupon>> listByProduct(
            @PathVariable Long productId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            List<SmsCoupon> couponList = memberCouponService.listByProduct(member.getId(), productId);
            return Result.success(couponList);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private UmsMember getMemberByUsername(String username) {
        if (username == null) {
            throw new RuntimeException("用户未登录");
        }
        
        UmsMember member = umsMemberMapper.selectByUsername(username);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return member;
    }
}
