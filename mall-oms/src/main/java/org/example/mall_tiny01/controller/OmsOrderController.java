package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.*;
import org.example.mall_tiny01.mbg.model.OmsOrder;
import org.example.mall_tiny01.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/order")
@ApiOperation("订单管理")
public class OmsOrderController {
    @Autowired
    private OmsOrderService orderService;

    @GetMapping("/list")
    @ApiOperation("查询订单列表")
    public Result list(
            @RequestParam(value = "orderSn", required = false) String orderSn,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "orderType", required = false) Integer orderType,
            @RequestParam(value = "sourceType", required = false) Integer sourceType,
            @RequestParam(value = "receiverKeyword", required = false) String receiverKeyword,
            @RequestParam(value = "createTime", required = false) String createTime,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize) {
        
        PageResult<OmsOrder> list = orderService.list(orderSn, status, orderType, 
                                                       sourceType, receiverKeyword, 
                                                       createTime, pageNum, pageSize);
        return Result.success(list);
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除订单")
    public Result delete(@RequestParam List<Long> ids) {
        orderService.delete(ids);
        return Result.success("操作成功");
    }

    @PostMapping("/update/close")
    @ApiOperation("批量关闭订单")
    public Result close(@RequestParam List<Long> ids, @RequestParam String note) {
        orderService.close(ids, note);
        return Result.success("操作成功");
    }

    @PostMapping("/update/delivery")
    @ApiOperation("批量发货")
    public Result delivery(@RequestBody List<OmsOrderDeliveryParam> deliveryParams) {
        orderService.delivery(deliveryParams);
        return Result.success("操作成功");
    }

    @PostMapping("/update/moneyInfo")
    @ApiOperation("修改订单费用信息")
    public Result updateMoneyInfo(@RequestBody OmsOrderMoneyParam moneyParam) {
        orderService.updateMoneyInfo(moneyParam);
        return Result.success("操作成功");
    }

    @PostMapping("/update/note")
    @ApiOperation("备注订单")
    public Result updateNote(@RequestParam Long id, @RequestParam String note, @RequestParam Integer status) {
        orderService.updateNote(id, note, status);
        return Result.success("操作成功");
    }

    @PostMapping("/update/receiverInfo")
    @ApiOperation("修改收货人信息")
    public Result updateReceiverInfo(@RequestBody OmsOrderReceiverInfoParam receiverInfoParam) {
        orderService.updateReceiverInfo(receiverInfoParam);
        return Result.success("操作成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("获取订单详情：订单信息、商品信息、操作记录")
    public Result getDetail(@PathVariable Long id) {
        OmsOrderDetail detail = orderService.getDetail(id);
        return Result.success(detail);
    }
}
