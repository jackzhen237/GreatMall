package org.example.mall_tiny01.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 下单关键指标埋点服务 —— 接入 Prometheus + Grafana 监控。
 *
 * 指标清单：
 * - order.generation.time   : 下单耗时（P50/P95/P99）
 * - order.success.total     : 下单成功总次数
 * - order.failed.total      : 下单失败总次数
 * - order.idempotent.reject.total : 幂等性拦截次数（重复提交）
 * - order.stock.insufficient.total: 库存不足次数
 */
@Service
public class MetricsService {

    private final Timer orderGenerationTimer;
    private final Counter orderSuccessCounter;
    private final Counter orderFailedCounter;
    private final Counter orderIdempotentRejectCounter;
    private final Counter stockInsufficientCounter;

    public MetricsService(MeterRegistry registry) {
        // 下单耗时 —— 记录 P50/P95/P99 分位数，用于发现慢请求
        this.orderGenerationTimer = Timer.builder("order.generation.time")
                .description("Order generation time in milliseconds")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        // 下单成功次数
        this.orderSuccessCounter = Counter.builder("order.success.total")
                .description("Total successful orders")
                .register(registry);

        // 下单失败次数
        this.orderFailedCounter = Counter.builder("order.failed.total")
                .description("Total failed orders")
                .register(registry);

        // 幂等性拦截次数（重复提交触发）
        this.orderIdempotentRejectCounter = Counter.builder("order.idempotent.reject.total")
                .description("Total idempotent rejected orders")
                .register(registry);

        // 库存不足失败次数
        this.stockInsufficientCounter = Counter.builder("order.stock.insufficient.total")
                .description("Total stock insufficient failures")
                .register(registry);
    }

    /**
     * 记录一次成功下单。
     *
     * @param durationMs 从进入 generateOrder 到返回的耗时（毫秒）
     */
    public void recordOrderSuccess(long durationMs) {
        orderSuccessCounter.increment();
        orderGenerationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录一次失败下单。
     *
     * @param reason     失败原因：idempotent / stock_insufficient / unknown
     * @param durationMs 耗时（毫秒）
     */
    public void recordOrderFailed(String reason, long durationMs) {
        orderFailedCounter.increment();
        orderGenerationTimer.record(durationMs, TimeUnit.MILLISECONDS);

        if ("idempotent".equals(reason)) {
            orderIdempotentRejectCounter.increment();
        } else if ("stock_insufficient".equals(reason)) {
            stockInsufficientCounter.increment();
        }
    }
}
