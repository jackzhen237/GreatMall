package org.example.mall_tiny01.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 浏览器缓存过滤器 — 给 GET 请求的响应加上 Cache-Control 头。
 *
 * 多级缓存第一层：
 * 浏览器看到 Cache-Control: max-age=60 后，60 秒内同样的请求不会再发到服务器，
 * 直接从浏览器本地缓存读取，响应时间接近 0ms。
 *
 * 缓存时间说明：
 * - 首页、商品详情等读多写少的数据：60 秒
 * - 用户个人数据（购物车、订单）不需要浏览器缓存，此过滤器只对 GET 生效
 */
@Component
public class CacheControlFilter implements Filter {

    /** 浏览器缓存时间（秒）：60 秒后过期，浏览器会重新请求服务器 */
    private static final int MAX_AGE_SECONDS = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 先执行后面的过滤器链和业务逻辑（Controller 先处理请求）
        chain.doFilter(request, response);

        // 业务处理完成后，给 GET 响应加上浏览器缓存头
        // 注意：只在 GET 请求时加，POST/PUT/DELETE 不加（写操作不应被缓存）
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if ("GET".equalsIgnoreCase(((jakarta.servlet.http.HttpServletRequest) request).getMethod())) {
            // Cache-Control: max-age=60 表示浏览器可以缓存此响应 60 秒
            // public 表示可以被浏览器和中间代理（如 CDN、Nginx）缓存
            httpResponse.setHeader("Cache-Control", "public, max-age=" + MAX_AGE_SECONDS);
        }
    }
}
