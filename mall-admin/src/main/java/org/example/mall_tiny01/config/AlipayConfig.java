package org.example.mall_tiny01.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝沙箱配置 —— 绑定 application.yml 中 alipay.* 的属性。
 *
 * 使用方式：@Autowired AlipayConfig config，然后 config.getAppId() 等。
 */
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /** 应用 ID（沙箱环境） */
    private String appId;
    /** 应用私钥（PKCS8 格式） */
    private String appPrivateKey;
    /** 支付宝公钥 */
    private String alipayPublicKey;
    /** 网关地址（沙箱为 openapi-sandbox.dl.alipaydev.com） */
    private String gatewayUrl;
    /** 异步通知地址：用户支付成功后支付宝调用的接口 */
    private String notifyUrl;
    /** 同步跳转地址：用户支付完成后跳转的页面 */
    private String returnUrl;
    /** 请求格式，固定 json */
    private String format = "json";
    /** 字符集，固定 utf-8 */
    private String charset = "utf-8";
    /** 签名方式，固定 RSA2 */
    private String signType = "RSA2";

    // ======================== getter / setter ========================

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppPrivateKey() { return appPrivateKey; }
    public void setAppPrivateKey(String appPrivateKey) { this.appPrivateKey = appPrivateKey; }
    public String getAlipayPublicKey() { return alipayPublicKey; }
    public void setAlipayPublicKey(String alipayPublicKey) { this.alipayPublicKey = alipayPublicKey; }
    public String getGatewayUrl() { return gatewayUrl; }
    public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getCharset() { return charset; }
    public void setCharset(String charset) { this.charset = charset; }
    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
}
