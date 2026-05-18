package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alipay")
public class AlipayController {
    @Autowired
    private AlipayService alipayService;

    @GetMapping("/pay")
    @ApiOperation("支付宝电脑网站支付")
    public String pay(
            @RequestParam(value = "outTradeNo", required = false) String outTradeNo,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "totalAmount", required = false) String totalAmount) {
        try {
            return alipayService.pay(outTradeNo, subject, totalAmount);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/webPay")
    @ApiOperation("支付宝手机网站支付")
    public String webPay(
            @RequestParam(value = "outTradeNo", required = false) String outTradeNo,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "totalAmount", required = false) String totalAmount) {
        try {
            return alipayService.webPay(outTradeNo, subject, totalAmount);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/query")
    @ApiOperation("支付宝统一收单线下交易查询")
    public Result<String> query(
            @RequestParam(value = "outTradeNo", required = false) String outTradeNo,
            @RequestParam(value = "tradeNo", required = false) String tradeNo) {
        try {
            String tradeStatus = alipayService.query(outTradeNo, tradeNo);
            return Result.success(tradeStatus);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }

    @PostMapping("/notify")
    @ApiOperation("支付宝异步回调")
    public String notify(HttpServletRequest request) {
        try {
            return alipayService.notify(request);
        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }
    }
}