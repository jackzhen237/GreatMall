package org.example.mall_tiny01.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 布隆过滤器工具类
 * 作用：在查询缓存/数据库前，先判断数据是否存在，防止恶意请求穿透到数据库。
 * 场景：海量数据（如千万级商品 ID），且数据删除操作很少的场景。
 */
@Component
public class BloomFilterUtil {

    private final RedissonClient redissonClient;

    public BloomFilterUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 初始化布隆过滤器
     *
     * @param name              过滤器名称（对应 Redis 的 Key）
     * @param expectedInsertions 预计放入的元素数量
     * @param falsePositiveProbability 误判率（0.01 表示 1% 的误判率，误判率越低占用内存越大）
     * @return 是否初始化成功（如果已存在则返回 false）
     */
    public boolean init(String name, long expectedInsertions, double falsePositiveProbability) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(name);
        // tryInit：如果过滤器已存在则不重新初始化，保护已有数据
        return bloomFilter.tryInit(expectedInsertions, falsePositiveProbability);
    }

    /**
     * 向过滤器中添加元素
     * 注意：必须在数据写入数据库的同时，调用此方法同步添加到布隆过滤器
     */
    public boolean add(String name, String value) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(name);
        return bloomFilter.add(value);
    }

    /**
     * 判断元素是否可能存在
     * @return true: 可能存在（需继续查缓存/数据库），false: 绝对不存在（直接拦截）
     */
    public boolean mightContain(String name, String value) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(name);
        return bloomFilter.contains(value);
    }
}