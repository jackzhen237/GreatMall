package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.dto.SmsCouponHistoryDetail;
import org.example.mall_tiny01.feign.PmsFeignClient;
import org.example.mall_tiny01.feign.SmsFeignClient;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.mbg.model.SmsCoupon;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;
import org.example.mall_tiny01.mbg.model.SmsCouponProductCategoryRelation;
import org.example.mall_tiny01.mbg.model.SmsCouponProductRelation;
import org.example.mall_tiny01.service.MemberCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MemberCouponServiceimpl implements MemberCouponService {

    @Autowired
    private SmsFeignClient smsFeignClient;

    @Autowired
    private PmsFeignClient pmsFeignClient;

    @Override
    public void add(Long memberId, Long couponId) {
        SmsCoupon coupon = smsFeignClient.getCouponSimple(couponId).getData();
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        SmsCouponHistory history = new SmsCouponHistory();
        history.setCouponId(couponId);
        history.setMemberId(memberId);
        history.setCouponCode(coupon.getCode());
        history.setGetType(1);
        history.setCreateTime(new Date());
        history.setUseStatus(0);

        smsFeignClient.addCouponHistory(history);
    }

    @Override
    public List<SmsCoupon> list(Long memberId, Integer useStatus) {
        List<SmsCouponHistory> historyList = smsFeignClient.listCouponHistory(null, useStatus).getData();

        List<SmsCoupon> couponList = new ArrayList<>();
        for (SmsCouponHistory history : historyList) {
            if (history.getMemberId().equals(memberId)) {
                SmsCoupon coupon = smsFeignClient.getCouponSimple(history.getCouponId()).getData();
                if (coupon != null) {
                    couponList.add(coupon);
                }
            }
        }

        return couponList;
    }

    @Override
    public List<SmsCouponHistory> listHistory(Long memberId, Integer useStatus) {
        List<SmsCouponHistory> historyList = smsFeignClient.listCouponHistory(null, useStatus).getData();

        List<SmsCouponHistory> resultList = new ArrayList<>();
        for (SmsCouponHistory history : historyList) {
            if (history.getMemberId().equals(memberId)) {
                resultList.add(history);
            }
        }

        return resultList;
    }

    @Override
    public List<SmsCouponHistoryDetail> listCart(Long memberId, Integer type) {
        List<SmsCouponHistory> historyList = smsFeignClient.listCouponHistory(null, type).getData();

        List<SmsCouponHistoryDetail> detailList = new ArrayList<>();
        for (SmsCouponHistory history : historyList) {
            if (history.getMemberId().equals(memberId)) {
                SmsCouponHistoryDetail detail = new SmsCouponHistoryDetail();
                detail.setId(history.getId());
                detail.setCouponId(history.getCouponId());
                detail.setMemberId(history.getMemberId());
                detail.setCouponCode(history.getCouponCode());
                detail.setGetType(history.getGetType());
                detail.setCreateTime(history.getCreateTime());
                detail.setUseStatus(history.getUseStatus());
                detail.setUseTime(history.getUseTime());
                detail.setOrderId(history.getOrderId());
                detail.setOrderSn(history.getOrderSn());

                SmsCoupon coupon = smsFeignClient.getCouponSimple(history.getCouponId()).getData();
                detail.setCoupon(coupon);

                List<SmsCouponProductRelation> productRelations = smsFeignClient.listCouponProductRelations(history.getCouponId()).getData();
                detail.setProductRelationList(productRelations);

                List<SmsCouponProductCategoryRelation> categoryRelations = smsFeignClient.listCouponCategoryRelations(history.getCouponId()).getData();
                detail.setCategoryRelationList(categoryRelations);

                detailList.add(detail);
            }
        }

        return detailList;
    }

    @Override
    public List<SmsCoupon> listByProduct(Long memberId, Long productId) {
        List<SmsCouponHistory> historyList = smsFeignClient.listCouponHistory(null, 0).getData();

        List<SmsCoupon> couponList = new ArrayList<>();
        for (SmsCouponHistory history : historyList) {
            if (history.getMemberId().equals(memberId) && history.getUseStatus() == 0) {
                SmsCoupon coupon = smsFeignClient.getCouponSimple(history.getCouponId()).getData();
                if (coupon != null) {
                    // useType: 0->全场通用; 1->指定分类; 2->指定商品
                    if (coupon.getUseType() == 0) {
                        couponList.add(coupon);
                    } else if (coupon.getUseType() == 1) {
                        PmsProduct product = pmsFeignClient.getProduct(productId).getData();
                        if (product != null && product.getProductCategoryId() != null) {
                            List<SmsCouponProductCategoryRelation> categoryRelations = smsFeignClient.listCouponCategoryRelations(coupon.getId()).getData();
                            for (SmsCouponProductCategoryRelation relation : categoryRelations) {
                                if (relation.getProductCategoryId().equals(product.getProductCategoryId())) {
                                    couponList.add(coupon);
                                    break;
                                }
                            }
                        }
                    } else if (coupon.getUseType() == 2) {
                        List<SmsCouponProductRelation> relations = smsFeignClient.listCouponProductRelations(coupon.getId()).getData();
                        for (SmsCouponProductRelation relation : relations) {
                            if (relation.getProductId().equals(productId)) {
                                couponList.add(coupon);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return couponList;
    }
}
