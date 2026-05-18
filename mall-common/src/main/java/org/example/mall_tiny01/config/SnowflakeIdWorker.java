package org.example.mall_tiny01.config;

import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 雪花算法 ID 生成器 —— 生成全局唯一、趋势递增的 64 位 Long 型 ID。
 *
 * 64 位结构（高位 → 低位）：
 * 1 位符号位（始终为 0）| 41 位时间戳（毫秒，从 START_TIMESTAMP 开始）| 10 位机器 ID | 12 位序列号
 *
 * 性能：单机每秒可生成约 409.6 万个 ID，适合订单号、分布式主键等高并发场景。
 * 机器 ID 自动从本机 IP 后两段哈希生成，无需手动配置。
 */
@Component
public class SnowflakeIdWorker {

    /** 起始时间戳（2024-01-01 00:00:00），41 位时间戳可用约 69 年 */
    private static final long START_TIMESTAMP = 1704067200000L;

    /** 机器 ID 占 10 位（支持 0~1023 共 1024 个节点） */
    private static final long WORKER_ID_BITS = 10L;

    /** 序列号占 12 位（同一毫秒内最多 4096 个 ID） */
    private static final long SEQUENCE_BITS = 12L;

    /** 最大机器 ID = 1023 */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 序列号掩码 = 4095，用于取低 12 位 */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** 机器 ID 左移 12 位（序列号在低 12 位） */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 时间戳左移 22 位（12 位序列号 + 10 位机器 ID） */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 上次生成 ID 的时间戳（毫秒） */
    private long lastTimestamp = -1L;

    /** 当前毫秒内的序列号（0~4095） */
    private long sequence = 0L;

    /** 当前节点的机器 ID */
    private final long workerId;

    public SnowflakeIdWorker() {
        this.workerId = generateWorkerId();
    }

    /**
     * 根据本机 IP 自动生成机器 ID。
     * 取 IP 后两段（如 192.168.1.5 → 取 1 和 5 → 组合成 261），再对 1024 取模。
     * 若获取 IP 失败，随机生成一个机器 ID。
     */
    private long generateWorkerId() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            byte[] ip = address.getAddress();
            // 取 IP 后两段（例如 192.168.1.5 → ip[2]=1, ip[3]=5 → 组合 = 261）
            int ipHash = ((ip[2] & 0xFF) << 8) | (ip[3] & 0xFF);
            return ipHash % (MAX_WORKER_ID + 1);
        } catch (UnknownHostException e) {
            // 获取 IP 失败时随机生成，保证服务能正常启动
            return (long) (Math.random() * (MAX_WORKER_ID + 1));
        }
    }

    /**
     * 生成下一个全局唯一 ID（线程安全）。
     * 格式：时间戳差值（41位） + 机器ID（10位） + 序列号（12位）
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        // 时钟回拨检测：如果当前时间小于上次生成 ID 的时间，拒绝生成
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException(
                    "时钟回拨，拒绝生成 ID。回拨时长: " + (lastTimestamp - currentTimestamp) + "ms");
        }

        // 同一毫秒内：序列号自增
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号到达上限（4095）→ 等待到下一毫秒
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒：序列号重置为 0
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        // 拼接三段数据：时间戳差值 | 机器ID | 序列号
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 自旋等待到下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long currentTimestamp = System.currentTimeMillis();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = System.currentTimeMillis();
        }
        return currentTimestamp;
    }

    public long getWorkerId() {
        return workerId;
    }
}
