# mall_tiny01 — 企业级微服务电商平台

基于 Spring Cloud Alibaba 的微服务电商系统，具备分布式事务、多级缓存、高可用架构、搜索引擎优化等企业级特性。

## 架构图

```
                          ┌─────────────────────────────────────────┐
                          │            Nginx (OpenResty)             │
                          │         Lua + Redis 多级缓存代理          │
                          │              Port :80                    │
                          └────────────┬────────────────────────────┘
                                       │
                                       ▼
                          ┌─────────────────────────────────────────┐
                          │       Spring Cloud Gateway              │
                          │          Sentinel 限流 / 路由            │
                          │            Port :8090                   │
                          └────────┬──────────┬──────────┬──────────┘
                                   │          │          │
                    ┌──────────────┼──────────┼──────────┼──────────────┐
                    │              │          │          │              │
                    ▼              ▼          ▼          ▼              ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
        │  mall-pms    │ │  mall-oms    │ │  mall-ums    │ │  mall-sms    │
        │  商品服务     │ │  订单服务     │ │  用户服务     │ │  营销服务     │
        │  Port:8081   │ │  Port:8082   │ │  Port:8083   │ │  Port:8084   │
        └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
               │                │                │                │
               └────────────────┼────────────────┼────────────────┘
                                │                │
                    ┌───────────┼────────────────┼───────────┐
                    │           │                │           │
                    ▼           ▼                ▼           ▼
        ┌──────────────┐ ┌──────────────────┐ ┌──────────────────┐
        │  mall-cms    │ │     Nacos 3.2.1  │ │  Nacos 2.2.3     │
        │  内容服务     │ │   服务发现/配置    │ │   Seata 注册专用   │
        │  Port:8086   │ │   Port:8848      │ │   Port:8850      │
        └──────────────┘ └──────────────────┘ └────────┬─────────┘
                                                       │
                                                       ▼
                                              ┌──────────────────┐
                                              │   Seata Server   │
                                              │   AT 模式 2.6.0  │
                                              │   Port:8091      │
                                              └──────────────────┘
```

### 基础设施架构

```
┌─────────────────────────────────────────────────────┐
│                    Linux VM (192.168.158.130)        │
│                                                      │
│  ┌─────────────────────┐  ┌─────────────────────┐   │
│  │      MySQL 8.0       │  │   Redis Sentinel     │   │
│  │  ┌───────┐ ┌───────┐ │  │  ┌───────┐          │   │
│  │  │Master │ │Slave  │ │  │  │Master │          │   │
│  │  │ :3306 │ │ :3307 │ │  │  │ :6379 │          │   │
│  │  └───────┘ └───────┘ │  │  └───┬───┘          │   │
│  │                      │  │  ┌───┼───┐          │   │
│  │                      │  │  ▼   ▼   ▼          │   │
│  │                      │  │ Slave Slave Slave    │   │
│  │                      │  │:6380 :6381 :6382     │   │
│  │                      │  │                      │   │
│  │                      │  │ Sentinel x3          │   │
│  │                      │  │:26379 :26380 :26381  │   │
│  └─────────────────────┘  └─────────────────────┘   │
│                                                      │
│  ┌─────────────────────┐                             │
│  │ Elasticsearch 7.17.3│ (Windows 本机 127.0.0.1)   │
│  │ + IK 中文分词       │                             │
│  │ + Pinyin 拼音插件    │                             │
│  │ Port:9200           │                             │
│  └─────────────────────┘                             │
└─────────────────────────────────────────────────────┘
```

### 多级缓存架构

```
用户请求
  │
  ▼
Nginx (OpenResty + Lua)
  │── 命中 ──→ 直接返回 (< 1ms)
  │
  ▼
Spring Boot 应用
  │── Caffeine 本地缓存 ──→ 命中返回 (微秒级)
  │── Redis ──→ 命中返回 (毫秒级)
  │── Elasticsearch ──→ 命中返回 (搜索场景)
  │── MySQL ──→ 回源查库
```

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 框架 | Spring Boot 3.2.4 + Spring Cloud 2023.0.1 | 微服务基础 |
| 网关 | Spring Cloud Gateway | 路由转发 + Sentinel 限流 |
| 注册/配置 | Nacos 3.2.1 + Nacos 2.2.3 | 服务发现 + Seata 注册 |
| 远程调用 | OpenFeign + LoadBalancer | 声明式 HTTP 客户端 |
| 分布式事务 | Seata 2.6.0 AT 模式 | 跨服务数据一致性 |
| 限流熔断 | Sentinel | QPS 限流 + 异常比例/慢调用熔断 |
| 本地缓存 | Caffeine | 微秒级响应，防 Redis 击穿 |
| 分布式缓存 | Redis Sentinel (1主3从3哨兵) | 高可用缓存 |
| 搜索引擎 | Elasticsearch 7.17.3 | 拼音分词 + IK 中文分词 |
| 数据库 | MySQL 8.0 主从复制 (1主1从) | 读写分离 |
| 对象存储 | MinIO | 图片/文件存储 |
| 监控 | Actuator + Micrometer + Prometheus | 指标埋点 + Grafana 可视化 |
| 订单号 | 雪花算法 (SnowflakeIdWorker) | 全局唯一 + 趋势递增 |
| 支付 | 支付宝沙箱 | 电脑网站支付 + 手机网站支付 |
| 定时任务 | Spring @Scheduled | 订单超时自动取消 |

## 模块结构

```
mall_tiny01
├── mall-gateway      # API 网关（Spring Cloud Gateway）
├── mall-common       # 公共模块（Redis/ES/Seata/JWT/MinIO 配置）
├── mall-mbg          # MyBatis Generator（Mapper + Model + XML）
├── mall-api          # Feign 接口定义
├── mall-pms          # 商品服务（品牌/分类/属性/SKU）
├── mall-oms          # 订单服务（订单/购物车/退货）
├── mall-ums          # 用户服务（登录/注册/会员/权限）
├── mall-sms          # 营销服务（优惠券/秒杀/首页推荐）
├── mall-cms          # 内容服务（专题/优选）
└── mall-admin        # 后台聚合模块
```

## 核心特性

### 1. 下单防超卖 — 5 层防护

```
第 1 层：前端 UUID 防重复提交
第 2 层：Redis SETNX 幂等键（5 分钟去重）
第 3 层：批量 CASE WHEN 数据库乐观锁（stock >= quantity）
第 4 层：Seata 全局事务回滚（跨服务一致性）
第 5 层：Sentinel 限流 + 排队等待（100 QPS + 500ms 超时）
```

### 2. 多级缓存

```
Caffeine（JVM 本地）→ Redis → ES → MySQL
    微秒级            毫秒级    搜索   回源
```

| 缓存级别 | 使用场景 | 策略 |
|---------|---------|------|
| Normal | 普通 CRUD（分类列表、属性列表） | Caffeine → Redis → MySQL |
| Hot | 高流量接口（商品详情、品牌详情） | 分布式锁防击穿，永不过期 |
| ExtremelyHot | 首页热点 | 逻辑过期 + 异步刷新，永不阻塞用户 |

### 3. 服务熔断

```
PMS 库存扣减 Feign 调用：
  异常比例 > 50%（1 分钟窗口） → 熔断 10 秒
  慢调用比例 > 50%（响应 > 1s） → 熔断 5 秒
  熔断期间 → 直接降级，不阻塞调用方线程
```

### 4. ES 搜索优化

- 拼音分词器（`my_pinyin_analyzer`）：输 `shouji` 搜到"手机"
- IK 中文分词（`ik_smart` / `ik_max_word`）
- LIKE 查询全部替换为 ES 全文搜索
- 搜索缓存 Key 包含分页参数，防止数据冲突

### 5. 数据一致性（双写）

```
CUD 操作顺序：
  MySQL 更新 → ES 同步（失败记日志，不阻断） → 删除 Redis/Caffeine 缓存
```

## 快速启动

### 前置条件

1. JDK 21
2. MySQL 8.0（已配置主从，Master :3306 / Slave :3307）
3. Redis Sentinel（:6379 主，:6380-6382 从，:26379-26381 哨兵）
4. Elasticsearch 7.17.3（已安装 IK + Pinyin 插件）
5. Nacos 3.2.1（:8848）+ Nacos 2.2.3（:8850，Seata 专用）
6. Seata Server 2.6.0（:8091）

### 启动顺序

```
1. 启动中间件（MySQL / Redis / ES / Nacos / Seata）
2. 按需启动业务模块：
   PmsApplication(:8081) → OmsApplication(:8082) → UmsApplication(:8083) → ...
3. 启动网关：GatewayApplication(:8090)
4. 访问：http://localhost:8090/product/brand/listAll
```

### 单体模式（开发调试用）

直接启动 `MallTiny01Application(:8085)`，所有模块在同一个 JVM 内运行。

## 面试要点

| 面试问题 | 回答关键点 |
|---------|-----------|
| 为什么用 Seata 不用 MQ？ | 下单需要实时扣库存 + 同步返回结果，AT 模式适合强一致性短事务 |
| 缓存穿透/击穿/雪崩怎么解决？ | 穿透→布隆过滤器+空值缓存，击穿→分布式锁，雪崩→随机TTL |
| 为什么用 Sentinel 排队等待而不是直接拒绝？ | 下单是核心交易链路，排队 500ms 等一个许可比直接拒绝丢单更合理 |
| Redis 哨兵怎么保证高可用？ | 3 哨兵监控，主挂了自动选举新主，客户端通过哨兵发现新主，无停机 |

## 数据库设计

核心表：`ums_admin` / `ums_member` / `pms_product` / `pms_sku_stock` / `oms_order` / `oms_order_item` / `sms_coupon` / `sms_coupon_history` / `cms_subject` 等 80+ 张表。

## 附录

- **Seata 配置修复**：Seata 2.6.0 自带 `nacos-client 1.4.6` 不支持 Nacos 3.x v1 命名 API。解决方案：部署 Nacos 2.2.3（:8850）作为 Seata 注册中心。
- **BCrypt 密码加密**：Admin/Member 登录使用 `BCryptPasswordEncoder`。数据库存储 `$2a$10$...` 哈希值，与明文比对使用 `matches()` 方法。
- **Sentinel 规则初始化**：`SentinelRuleConfig.java` 在启动时自动加载限流和熔断规则。
