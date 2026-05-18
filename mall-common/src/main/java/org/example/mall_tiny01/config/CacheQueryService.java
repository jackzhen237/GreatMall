package org.example.mall_tiny01.config;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 统一缓存查询服务——所有业务查询的唯一入口。
 *
 * 多级缓存流程（自动）：
 * 1. 查 Caffeine（本地内存，纳秒级）
 * 2. 未命中 → 查 Redis（分布式缓存）
 * 3. 未命中 → 查数据库（走你提供的 Supplier）
 * 4. 查到数据后自动回填 Caffeine + Redis
 *
 * 使用示例：
 * {@code
 * PmsProduct product = cacheService.query(
 *     "product:" + id,           // 缓存 key
 *     PmsProduct.class,           // 返回值类型
 *     () -> mapper.selectById(id) // 查数据库的逻辑
 * );
 * }
 */
@Service
public class CacheQueryService {

    private final CaffeineCacheUtil caffeine;
    private final RedisConfig redis;

    public CacheQueryService(CaffeineCacheUtil caffeine, RedisConfig redis) {
        this.caffeine = caffeine;
        this.redis = redis;
    }

    /**
     * 查询数据（自动走 Caffeine → Redis → DB 三级缓存）。
     *
     * @param key        缓存键，建议命名规则 "模块:ID"，如 "product:1"、"brand:5"
     * @param type       返回值类型（用于 JSON 反序列化），传 {@code Product.class} 即可
     * @param dbSupplier 查数据库的代码，传一个 lambda，如 {@code () -> mapper.selectById(id)}
     * @return 查询结果，数据库没数据时返回 null
     */
    public <T> T query(String key, Class<T> type, Supplier<T> dbSupplier) {
        // 第1层：查 Caffeine
        String cached = caffeine.get(key, k -> {
            // 第2层：Caffeine 未命中 → 查 Redis
            String redisVal = redis.getNormal(k, redisKey -> {
                // 第3层：Redis 未命中 → 查数据库
                T dbResult = dbSupplier.get();
                if (dbResult == null) {
                    return null;  // 数据库也没有，Redis 会缓存空字符串防穿透
                }
                // 对象转 JSON 存入 Redis
                return JSON.toJSONString(dbResult);
            });
            return redisVal;
        });

        if (cached == null || cached.isEmpty()) {
            return null;
        }
        // JSON 反序列化为目标类型
        return JSON.parseObject(cached, type);
    }

    /**
     * 查询数据（热点模式）。
     * 与 query 的区别：缓存未命中时加分布式锁，只让一个请求查库，其余等待。
     * 适合：秒杀商品、热门品牌等并发量大的数据。
     */
    public <T> T queryHot(String key, Class<T> type, Supplier<T> dbSupplier) {
        String cached = caffeine.get(key, k -> {
            String redisVal = redis.getHot(k, redisKey -> {
                T dbResult = dbSupplier.get();
                if (dbResult == null) {
                    return null;
                }
                return JSON.toJSONString(dbResult);
            });
            return redisVal;
        });

        if (cached == null || cached.isEmpty()) {
            return null;
        }
        return JSON.parseObject(cached, type);
    }

    /**
     * 查询数据（超热点模式 - 逻辑过期方案）。
     * 与 queryHot 的区别：缓存逻辑过期后直接返回旧数据，由抢到锁的请求异步刷新。
     * 用户无感知等待，适合瞬时 10 万+ QPS 的极端热点场景（如首页推荐、爆款商品）。
     * 
     * 注意：首次写入需调用 setExtremelyHot() 方法，后续自动维护逻辑过期时间。
     */
    public <T> T queryExtremelyHot(String key, Class<T> type, Supplier<T> dbSupplier) {
        String cached = caffeine.get(key, k -> {
            String redisVal = redis.getExtremelyHot(k, redisKey -> {
                T dbResult = dbSupplier.get();
                if (dbResult == null) {
                    return null;
                }
                return JSON.toJSONString(dbResult);
            });
            return redisVal;
        });

        if (cached == null || cached.isEmpty()) {
            return null;
        }
        return JSON.parseObject(cached, type);
    }

    /**
     * 写入缓存（数据库更新后同步调用，保持缓存与数据库一致）。
     * 只更新 Caffeine + Redis，不查数据库。
     * 
     * 注意：Redis 设置 30-35 分钟随机过期（防雪崩），Caffeine 固定 5 分钟过期。
     */
    public <T> void put(String key, T value) {
        String json = JSON.toJSONString(value);
        caffeine.put(key, json);
        // 普通数据带随机 TTL，避免大量 key 同时过期导致雪崩
        long ttlMinutes = 30 + (long) (Math.random() * 6);
        redis.set(key, json, ttlMinutes, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 写入热点缓存（永不过期，靠主动更新或手动删除刷新）。
     * 配合 queryHot() 使用，适合秒杀商品、热门品牌等高并发数据。
     * 
     * @param key   缓存键
     * @param value 业务数据对象
     */
    public <T> void putHot(String key, T value) {
        String json = JSON.toJSONString(value);
        caffeine.put(key, json);
        redis.setHot(key, json);  // 热点数据永不过期
    }

    /**
     * 写入超热点缓存（带逻辑过期时间）。
     * 配合 queryExtremelyHot() 使用，首次初始化或手动刷新时调用。
     * 
     * @param key   缓存键
     * @param value 业务数据对象
     */
    public <T> void putExtremelyHot(String key, T value) {
        String json = JSON.toJSONString(value);
        caffeine.put(key, json);
        redis.setExtremelyHot(key, json);
    }

    /**
     * 删除缓存（数据库数据被删除后调用）。
     */
    public void invalidate(String key) {
        caffeine.invalidate(key);
        redis.delete(key);
    }

    /**
     * 查看所有层级的缓存统计。
     * 返回格式示例：Caffeine[hitRate=0.95] Redis[keys=120]
     */
    public String stats() {
        return "Caffeine[" + caffeine.stats() + "]";
    }
}
