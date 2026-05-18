package org.example.mall_tiny01.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AlipayService {
    String pay(String outTradeNo, String subject, String totalAmount);

    String webPay(String outTradeNo, String subject, String totalAmount);

    String query(String outTradeNo, String tradeNo);

    String notify(HttpServletRequest request);
}
