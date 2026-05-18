package org.example.mall_tiny01.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.dto.ConfirmOrderResult;
import org.example.mall_tiny01.dto.OmsOrderDetail;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.feign.UmsFeignClient;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/order")
@Api(tags = "订单管理")
public class OmsPortalOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UmsFeignClient umsFeignClient;

    @PostMapping("/generateConfirmOrder")
    @ApiOperation("根据购物车信息生成确认单")
    public Result<ConfirmOrderResult> generateConfirmOrder(@RequestBody List<Long> cartIds) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            ConfirmOrderResult result = orderService.generateConfirmOrder(member.getId(), cartIds);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 生成订单（Sentinel 限流保护 + 排队等待）。
     *
     * 限流规则在 SentinelRuleConfig 中，启动时自动加载：
     * - 限流模式：直接（针对此接口）
     * - 限流效果：排队等待（Rate Limiter，FIFO 队列平滑通过）
     * - QPS 阈值：100/秒
     * - 最大排队：500ms，超时触发 blockHandler
     */
    @ApiOperation("生成订单")
    @PostMapping("/generate")
    @SentinelResource(value = "generateOrder" , blockHandler = "orderRateLimitBlock")
    public Result<Long> generateOrder(
            @RequestParam("cartIds") List<Long> cartIds,
            @RequestParam("couponId") Long couponId,
            @RequestParam("memberReceiveAddressId") Long memberReceiveAddressId,
            @RequestParam("payType") Integer payType,
            @RequestParam("useIntegration") Integer useIntegration,
            @RequestParam("orderUuid") String orderUuid) {

        Long memberId = UserContext.getCurrentMemberId();
        Long orderId = orderService.generateOrder(memberId, cartIds, couponId,
                memberReceiveAddressId, payType, useIntegration, orderUuid);
        return Result.success(orderId);
    }

    /**
     * Sentinel 限流降级方法 —— 排队超时或 QPS 超阈值时自动调用。
     * 签名必须与原方法一致，末尾多加一个 BlockException。
     */
    public Result<Long> orderRateLimitBlock(
            List<Long> cartIds, Long couponId, Long memberReceiveAddressId,
            Integer payType, Integer useIntegration, String orderUuid,
            BlockException e) {
        return Result.error("当前下单人数过多，请稍后重试");
    }

    @GetMapping("/list")
    @ApiOperation("按状态分页获取用户订单列表")
    public Result<PageResult<OmsOrderDetail>> listOrder(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam Integer status) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            PageResult<OmsOrderDetail> pageResult = orderService.list(member.getId(),
                    status, pageNum, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/paySuccess")
    @ApiOperation("用户支付成功的回调")
    public Result paySuccess(@RequestParam Long orderId, @RequestParam Integer payType) {
        try {
            orderService.paySuccess(orderId, payType);
            return Result.success("支付成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancelUserOrder")
    @ApiOperation("用户可以取消自己的未发货订单")
    public Result cancelUserOrder(@RequestParam Long orderId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            orderService.cancelUserOrder(member.getId(), orderId);
            return Result.success("订单取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/confirmReceiveOrder")
    @ApiOperation("用户确认收货")
    public Result confirmReceiveOrder(@RequestParam Long orderId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            orderService.confirmReceiveOrder(member.getId(), orderId);
            return Result.success("确认收货成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/deleteOrder")
    @ApiOperation("用户删除已完成或已关闭的订单")
    public Result deleteUserOrder(@RequestParam Long orderId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            orderService.deleteOrder(member.getId(), orderId);
            return Result.success("订单删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{orderId}")
    @ApiOperation("获取订单详情")
    public Result<OmsOrderDetail> getOrderDetail(@PathVariable Long orderId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            OmsOrderDetail detail = orderService.getOrderDetail(member.getId(), orderId);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private UmsMember getMemberByUsername(String username) {
        return umsFeignClient.getMemberByUsername(username).getData();
    }
}
