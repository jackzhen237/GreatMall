package org.example.mall_tiny01.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 本地缓存工具类（基于 Caffeine），与 RedisConfig 配合形成多级缓存：
 *
 * 请求流程：浏览器缓存 → Nginx代理缓存 → Caffeine(本类) → Redis → 数据库
 *
 * Caffeine 是什么：
 * Java 进程内的本地缓存库，数据存在 JVM 堆内存里，读写速度是纳秒级（比 Redis 网络调用快 1000 倍）。
 * 但因为是进程内的，多个服务实例之间不共享，所以只作为第一层缓存，后面还有 Redis 兜底。
 *
 * 使用方式：
 * CaffeineCacheUtil.get(key, redisConfig::getNormal)
 * → 先查 Caffeine → 未命中则调用 redisConfig.getNormal（走 Redis → DB）→ 回填 Caffeine
 */
@Component
public class CaffeineCacheUtil {

    /**
     * Caffeine Cache 实例。
     *
     * 参数说明：
     * - maximumSize(10000)：最多缓存 10000 个 key，超出后 Caffeine 会用 W-TinyLFU 算法自动淘汰不常用的数据
     * - expireAfterWrite(5, TimeUnit.MINUTES)：每个 key 写入 5 分钟后自动过期
     *   本地缓存 TTL 设短一些，让 Redis 作为更权威的数据源，减少多实例数据不一致的时间窗口
     * - recordStats()：开启统计信息，方便排查缓存命中率
     */
    private final Cache<String, String> cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 从多级缓存中获取数据。
     *
     * 执行流程：
     * 1. 查 Caffeine（本地内存，纳秒级）
     * 2. 如果 Caffeine 未命中 → 调用 fallback 函数（通常是 redisConfig.getNormal / getHot）
     * 3. fallback 内部会查 Redis → Redis 未命中则查数据库 → 回填 Redis
     * 4. 把 fallback 返回的结果写入 Caffeine（5 分钟 TTL），下次请求直接命中本地缓存
     *
     * @param key      缓存键
     * @param fallback Caffeine 未命中时的回调函数，传入 key，返回 value。
     *                 通常传 {@code redisConfig::getNormal} 或自己写 lambda
     * @return 缓存数据，可能为 null（数据库也没有）
     */
    public String get(String key, Function<String, String> fallback) {
        // Caffeine.get(key, Function)：原子操作
        // 如果 key 存在 → 直接返回
        // 如果 key 不存在 → 调用 Function 计算值 → 自动写入缓存 → 返回
        return cache.get(key, fallback);
    }

    /**
     * 主动写入本地缓存。
     * 例如：数据更新后，可以同时更新 Caffeine，避免用户暂时读到旧数据。
     */
    public void put(String key, String value) {
        cache.put(key, value);
    }

    /**
     * 删除本地缓存中的一个 key。
     * 例如：数据库数据被修改后，调用此方法让下次请求走 fallback 拿最新数据。
     */
    public void invalidate(String key) {
        cache.invalidate(key);
    }

    /**
     * 清空所有本地缓存。
     * 慎用：会瞬间把所有请求打到 Redis，可能导致 Redis 压力暴增。
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * 获取缓存统计信息，用于监控和排查。
     * 返回格式示例：CacheStats{hitCount=950, missCount=50, hitRate=0.95, ...}
     */
    public String stats() {
        return cache.stats().toString();
    }
}
