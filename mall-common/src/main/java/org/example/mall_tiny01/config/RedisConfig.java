package org.example.mall_tiny01.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis 缓存工具类（基于 Redisson 客户端）。
 *
 * 核心对象说明：
 * - RedissonClient：Redisson 的入口，所有 Redis 操作都从它开始。由 redisson-spring-boot-starter 自动创建并注入。
 * - RBucket：Redisson 对 Redis String 类型的封装，用法相当于一个 key-value 的"桶"，支持 get/set/过期等操作。
 * - RLock：Redisson 的分布式锁，基于 Redis 实现，tryLock() 尝试加锁，unlock() 释放锁。
 *   优势：Redisson 的锁自带"看门狗"机制，获取锁后会自动续期，不用担心业务执行太久锁过期。
 *
 * 三种缓存策略：
 * 1. getNormal  — 普通数据：未命中则查库回填，带随机 TTL（防缓存雪崩）
 * 2. getHot     — 热点数据：未命中时加分布式锁，只让一个请求查库（防缓存击穿）
 * 3. getExtremelyHot — 极高热点：逻辑过期 + 异步刷新，旧数据直接返回不阻塞用户（防缓存击穿 + 高并发）
 */
@Component
public class RedisConfig {

    private final RedissonClient redissonClient;

    // RedissonClient 由构造器注入，Redisson 的自动配置会在启动时创建这个 Bean
    public RedisConfig(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // ======================== 基础操作 ========================

    /** 
     * 写入热点缓存（永不过期）。
     * 适合配合 getHot() 使用的高并发数据，靠主动更新或删除来刷新。
     */
    public void setHot(String key, String value) {
        redissonClient.getBucket(key).set(value);
    }

    /**
     * 带过期写入（严格使用传入的超时时间）。
     * 例如：验证码 set(key, code, 5, TimeUnit.MINUTES) → 5 分钟后自动删除。
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redissonClient.getBucket(key).set(value, Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * 简单读取，不做任何回源逻辑，直接从 Redis 取值。
     */
    public String get(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    // ======================== 普通缓存（防穿透）====================

    /**
     * 获取普通数据缓存。
     *
     * 流程：先查 Redis → 命中返回 → 未命中查数据库 → 回填 Redis（随机 TTL）
     *
     * 缓存穿透防护：数据库也查不到时，缓存空字符串 ""，短期（3~5分钟随机）不穿透到数据库。
     *
     * @param key     缓存键
     * @param dbQuery 查数据库的回调函数，你传一个 lambda 即可，如 {@code id -> mapper.selectById(id)}
     */
    public String getNormal(String key, Function<String, String> dbQuery) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        String value = bucket.get();  // 从 Redis 读取

        if (value != null) {
            return value;  // 命中缓存，直接返回
        }

        // 未命中，调用你传入的 dbQuery 函数查数据库
        value = dbQuery.apply(key);

        if (value == null) {
            // 数据库也没有 → 缓存空值，短期过期，防止缓存穿透
            long shortTtlSeconds = TimeUnit.SECONDS.convert(3 + (long) (Math.random() * 3), TimeUnit.MINUTES);
            bucket.set("", Duration.ofSeconds(shortTtlSeconds));
        } else {
            // 数据库有数据 → 回填缓存，30~35 分钟随机过期（防雪崩）
            long ttlSeconds = TimeUnit.SECONDS.convert(30 + (long) (Math.random() * 6), TimeUnit.MINUTES);
            bucket.set(value, Duration.ofSeconds(ttlSeconds));
        }

        return value;
    }

    // ======================== 热点缓存（防击穿）====================

    /**
     * 获取热点数据缓存（防缓存击穿）。
     *
     * 问题场景：热点 key 过期瞬间，大量请求同时打到数据库，可能压垮数据库。
     * 解决方案：第一个请求获取分布式锁，查库回填缓存；其余请求等待 50ms 后重试读缓存。
     *
     * @param key     缓存键
     * @param dbQuery 查数据库的回调函数
     */
    public String getHot(String key, Function<String, String> dbQuery) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        String value = bucket.get();
        if (value != null) {
            return value;  // 缓存命中，直接返回
        }

        // 缓存未命中 → 尝试获取分布式锁，锁的 key = "lock:" + 原始 key
        // getLock(key)：创建一个 RLock 分布式锁对象
        RLock lock = redissonClient.getLock("lock:" + key);
        try {
            // tryLock(10, TimeUnit.SECONDS)：尝试获取锁，最多等待 10 秒
            // 返回 true = 获取成功，false = 获取失败（其他线程持有锁）
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    // 获取到锁后再次检查缓存（双重检查），因为可能上一个持锁者已回填完毕
                    value = bucket.get();
                    if (value != null) {
                        return value;
                    }

                    // 缓存确实没有 → 查数据库
                    value = dbQuery.apply(key);

                    if (value == null) {
                        // 数据库也没有 → 缓存空值防穿透
                        long shortTtlSeconds = TimeUnit.SECONDS.convert(3 + (long) (Math.random() * 3), TimeUnit.MINUTES);
                        bucket.set("", Duration.ofSeconds(shortTtlSeconds));
                    } else {
                        // 热点数据永不过期，后续靠主动更新或业务方手动删除来刷新
                        bucket.set(value);
                    }
                } finally {
                    lock.unlock();  // 释放锁，即使上面抛异常也会释放
                }
            } else {
                // 没获取到锁 → 等待 50ms，让持锁者有时间查库回填，然后重试读缓存
                Thread.sleep(50);
                value = bucket.get();
            }
        } catch (InterruptedException e) {
            // 线程被中断时恢复中断状态，上层可感知
            Thread.currentThread().interrupt();
        }

        return value;
    }

    // ======================== 极高热点缓存（逻辑过期）====================

    /**
     * 写入极高热点缓存（配合 getExtremelyHot 使用）。
     *
     * 存储格式：JSON 字符串，包含两个字段：
     * - "data"：实际业务数据
     * - "expireTime"：逻辑过期时间戳（毫秒），固定为写入时间 + 30 分钟
     *
     * 关键：Redis 本身不设物理 TTL，靠 expireTime 字段判断是否过期。
     * 这样做的好处：缓存物理上永不过期，哪怕过期了也能返回旧数据，避免缓存击穿。
     */
    public void setExtremelyHot(String key, String value) {
        long logicalTtlMinutes = 30;
        // System.currentTimeMillis() 返回当前时间戳（毫秒），加上 30 分钟得到逻辑过期时间
        long expireTime = System.currentTimeMillis() + logicalTtlMinutes * 60 * 1000;

        // 组装成 { "data": "业务数据", "expireTime": 1716123456789 }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("data", value);
        result.put("expireTime", expireTime);

        // JSON.toJSONString(map)：把 Map 转成 JSON 字符串存入 Redis
        redissonClient.getBucket(key).set(JSON.toJSONString(result));
    }

    /**
     * 获取极高热点数据缓存（逻辑过期方案，适合瞬时 10 万+ QPS 的场景）。
     *
     * 与 getHot 的区别：
     * - getHot：缓存物理过期后，第一个请求查库，其余等待 → 用户会等待
     * - getExtremelyHot：缓存逻辑过期后，直接返回旧数据（不阻塞用户），由抢到锁的请求异步刷新
     *
     * TypeReference 说明：
     * fastjson 的 TypeReference 用于在反序列化时保留泛型信息。
     * 因为 JSON.parseObject 返回的 Map 默认是 Map<String, Object>，但 Java 泛型在编译后会擦除，
     * 所以需要 TypeReference<Map<String, Object>>() {}.getType() 来告诉 fastjson 具体类型。
     *
     * @param key     缓存键
     * @param dbQuery 查数据库的回调函数
     */
    public String getExtremelyHot(String key, Function<String, String> dbQuery) {
        long logicalTtlMinutes = 30;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String value = bucket.get();

        // ===== 情况 A：缓存物理上存在 =====
        if (value != null) {
            try {
                // 把 JSON 字符串解析回 Map
                // TypeReference：fastjson 用来保留泛型类型的方式，否则解析出来的 Map 不知道 key/value 的类型
                Map<String, Object> data = JSON.parseObject(value,
                        new TypeReference<Map<String, Object>>() {}.getType());
                Long expireTime = (Long) data.get("expireTime");
                String realData = (String) data.get("data");

                // 检查是否逻辑过期：expireTime 小于当前时间说明已过期
                if (expireTime != null && expireTime < System.currentTimeMillis()) {
                    // 逻辑已过期 → 尝试获取锁来刷新
                    // tryLock() 不传参数 = 立即返回，不等待（非阻塞）
                    // 获取不到锁说明已有其他请求在刷新，直接返回旧数据即可
                    RLock lock = redissonClient.getLock("lock:" + key);
                    if (lock.tryLock()) {
                        try {
                            // 获取到锁 → 双重检查：可能上个持锁者已刷新完毕
                            String cachedValue = bucket.get();
                            if (cachedValue != null) {
                                Map<String, Object> cachedData = JSON.parseObject(cachedValue,
                                        new TypeReference<Map<String, Object>>() {}.getType());
                                Long cachedExpireTime = (Long) cachedData.get("expireTime");
                                // 如果缓存已被刷新（expireTime > 当前时间），无需重复刷新
                                if (cachedExpireTime != null && cachedExpireTime > System.currentTimeMillis()) {
                                    return realData;
                                }
                            }

                            // 确实需要刷新 → 查数据库
                            String newData = dbQuery.apply(key);
                            if (newData != null) {
                                // 构建新数据，逻辑过期时间重新计算
                                long newExpireTime = System.currentTimeMillis() + logicalTtlMinutes * 60 * 1000;
                                Map<String, Object> result = new java.util.HashMap<>();
                                result.put("data", newData);
                                result.put("expireTime", newExpireTime);
                                bucket.set(JSON.toJSONString(result));
                                return newData;  // 返回最新数据
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
                // 不管逻辑是否过期，都返回旧数据（非阻塞，用户体验好）
                return realData;
            } catch (Exception e) {
                // JSON 解析失败，当作缓存未命中处理，走下面的加锁查库流程
            }
        }

        // ===== 情况 B：缓存物理上不存在（首次启动或缓存被手动删除）=====
        RLock lock = redissonClient.getLock("lock:" + key);
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    // 双重检查
                    value = bucket.get();
                    if (value != null) {
                        try {
                            Map<String, Object> data = JSON.parseObject(value,
                                    new TypeReference<Map<String, Object>>() {}.getType());
                            return (String) data.get("data");
                        } catch (Exception e) {
                            return value;
                        }
                    }

                    // 查数据库
                    String data = dbQuery.apply(key);
                    if (data != null) {
                        // 构建带逻辑过期时间的数据
                        long expireTime = System.currentTimeMillis() + logicalTtlMinutes * 60 * 1000;
                        Map<String, Object> result = new java.util.HashMap<>();
                        result.put("data", data);
                        result.put("expireTime", expireTime);
                        bucket.set(JSON.toJSONString(result));
                    } else {
                        // 数据库也没有 → 缓存空值防穿透
                        long shortTtlSeconds = TimeUnit.SECONDS.convert(3 + (long) (Math.random() * 3), TimeUnit.MINUTES);
                        bucket.set("", Duration.ofSeconds(shortTtlSeconds));
                    }
                    return data;
                } finally {
                    lock.unlock();
                }
            } else {
                // 没获取到锁 → 等 50ms 重试
                Thread.sleep(50);
                value = bucket.get();
                if (value != null) {
                    try {
                        Map<String, Object> data = JSON.parseObject(value,
                                new TypeReference<Map<String, Object>>() {}.getType());
                        return (String) data.get("data");
                    } catch (Exception e) {
                        return value;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return null;
    }

    // ======================== 辅助操作 ========================

    /** 删除缓存。delete() 直接删除 key，返回 true 表示删除成功。 */
    public Boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 判断 key 是否存在。
     * isExists() 返回 true/false，不会把数据取出来，比 get() 更轻量。
     */
    public Boolean hasKey(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 设置过期时间。
     * Duration.ofMillis(unit.toMillis(timeout))：先把 timeout + unit 转成毫秒，再用 Duration 包装。
     * 例如 expire("key", 30, TimeUnit.MINUTES) → Duration.ofMillis(30 * 60 * 1000)。
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redissonClient.getBucket(key).expire(Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * 获取剩余过期时间（毫秒）。
     * remainTimeToLive()：Redisson 方法，返回 key 还剩多少毫秒过期。
     * 返回值 ≤ 0 表示永不过期或 key 不存在，此时返回 null。
     */
    public Long getExpire(String key) {
        long remainTime = redissonClient.getBucket(key).remainTimeToLive();
        return remainTime > 0 ? remainTime : null;
    }
}
