package org.example.mall_tiny01.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.example.mall_tiny01.config.AlipayConfig;
import org.example.mall_tiny01.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayServiceimpl implements AlipayService {

    @Autowired
    private AlipayConfig alipayConfig;

    // 懒加载 AlipayClient，第一次调用时创建并缓存
    private volatile AlipayClient alipayClient;

    private AlipayClient getClient() {
        if (alipayClient == null) {
            synchronized (this) {
                if (alipayClient == null) {
                    alipayClient = new DefaultAlipayClient(
                            alipayConfig.getGatewayUrl(),
                            alipayConfig.getAppId(),
                            alipayConfig.getAppPrivateKey(),
                            alipayConfig.getFormat(),
                            alipayConfig.getCharset(),
                            alipayConfig.getAlipayPublicKey(),
                            alipayConfig.getSignType());
                }
            }
        }
        return alipayClient;
    }

    @Override
    public String pay(String outTradeNo, String subject, String totalAmount) {
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            // 异步通知地址：用户支付成功后支付宝通知后端
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            // 同步跳转地址：用户支付完成后浏览器跳转
            request.setReturnUrl(alipayConfig.getReturnUrl());

            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("total_amount", totalAmount);
            bizContent.put("subject", subject);
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

            request.setBizContent(JSON.toJSONString(bizContent));
            return getClient().pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝电脑支付请求失败", e);
        }
    }

    @Override
    public String webPay(String outTradeNo, String subject, String totalAmount) {
        try {
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setReturnUrl(alipayConfig.getReturnUrl());

            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("total_amount", totalAmount);
            bizContent.put("subject", subject);
            bizContent.put("product_code", "QUICK_WAP_WAY");

            request.setBizContent(JSON.toJSONString(bizContent));
            return getClient().pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝手机支付请求失败", e);
        }
    }

    @Override
    public String query(String outTradeNo, String tradeNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("trade_no", tradeNo);
            request.setBizContent(JSON.toJSONString(bizContent));

            AlipayTradeQueryResponse response = getClient().execute(request);
            return response.getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝查询订单失败", e);
        }
    }

    @Override
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> params.put(key, value[0]));

        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType());

            if (signVerified) {
                String tradeStatus = request.getParameter("trade_status");
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    return "success";
                }
            }
            return "failure";
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝异步通知验签失败", e);
        }
    }
}
