package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.ConfirmOrderResult;
import org.example.mall_tiny01.dto.OmsOrderDetail;
import org.example.mall_tiny01.dto.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

public interface OrderService {
    /**
     * 取消单个超时订单
     */
    void cancelOrder(Long orderId);

    /**
     * 自动取消超时订单（通常由定时任务调用）
     */
    void cancelTimeOutOrder();

    /**
     * 用户主动取消订单
     */
    void cancelUserOrder(Long memberId, Long orderId);

    /**
     * 用户确认收货
     */
    void confirmReceiveOrder(Long memberId, Long orderId);

    /**
     * 用户删除订单（逻辑删除）
     */
    void deleteOrder(Long memberId, Long orderId);

    /**
     * 根据订单ID获取订单详情
     */
    OmsOrderDetail getOrderDetail(Long memberId, Long orderId);

    /**
     * 根据购物车信息生成确认单
     */
    ConfirmOrderResult generateConfirmOrder(Long memberId, List<Long> cartIds);

    /**
     * 生成订单
     * @param orderUuid 前端生成的唯一请求 ID，用于幂等性校验
     */
    Long generateOrder(Long memberId, List<Long> cartIds, Long couponId, Long memberReceiveAddressId, Integer payType, Integer useIntegration, String orderUuid);

    /**
     * 按状态分页获取用户订单列表
     */
    PageResult<OmsOrderDetail> list(Long memberId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 用户支付成功的回调
     */
    void paySuccess(Long orderId, Integer payType);
}