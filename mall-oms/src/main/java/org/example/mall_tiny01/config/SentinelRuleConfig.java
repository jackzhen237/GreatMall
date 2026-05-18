package org.example.mall_tiny01.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流 + 熔断规则初始化。
 */
@Component
public class SentinelRuleConfig implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // ==================== 限流规则 ====================
        List<FlowRule> flowRules = new ArrayList<>();

        FlowRule flowRule = new FlowRule();
        flowRule.setResource("generateOrder");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(100);
        flowRule.setLimitApp("default");
        // 限流效果：排队等待（匀速排队，不直接拒绝）
        flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        // 最大排队 500ms，超过触发 blockHandler
        flowRule.setMaxQueueingTimeMs(500);
        flowRules.add(flowRule);

        FlowRuleManager.loadRules(flowRules);

        // ==================== 熔断规则（PMS 库存批量扣减） ====================
        List<DegradeRule> degradeRules = new ArrayList<>();

        // 熔断器 1：异常比例 —— 库存扣减的异常比例超过 50% 就熔断
        // 资源名 = Sentinel 自动生成的 Feign 调用资源名
        // 格式：HTTP方法:http://服务名/接口路径
        DegradeRule exceptionRule = new DegradeRule("POST:http://mall-pms/sku/stock/batchReduce");
        exceptionRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        // 异常比例阈值 0.5 = 50%
        exceptionRule.setCount(0.5);
        // 熔断 10 秒后进入半开状态（放一个请求试试，成功就关熔断）
        exceptionRule.setTimeWindow(10);
        // 最少 5 个请求才触发统计（避免低流量误触发）
        exceptionRule.setMinRequestAmount(5);
        // 1 分钟统计窗口
        exceptionRule.setStatIntervalMs(60000);
        degradeRules.add(exceptionRule);

        // 熔断器 2：慢调用比例 —— 50% 的库存调用超过 1 秒就提前熔断
        DegradeRule slowRtRule = new DegradeRule("batchReduceStock");
        slowRtRule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());
        // 响应时间超过 1000ms 算"慢调用"
        slowRtRule.setCount(1000);
        // 慢调用比例阈值 0.5 = 50%
        slowRtRule.setSlowRatioThreshold(0.5);
        // 熔断 5 秒
        slowRtRule.setTimeWindow(5);
        // 最少 5 个请求
        slowRtRule.setMinRequestAmount(5);
        // 1 分钟统计窗口
        slowRtRule.setStatIntervalMs(60000);
        degradeRules.add(slowRtRule);

        DegradeRuleManager.loadRules(degradeRules);
    }
}
