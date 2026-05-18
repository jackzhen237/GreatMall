package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.*;
import org.example.mall_tiny01.feign.PmsFeignClient;
import org.example.mall_tiny01.feign.UmsFeignClient;
import org.example.mall_tiny01.mbg.mapper.OmsCartItemMapper;
import org.example.mall_tiny01.mbg.mapper.OmsOrderItemMapper;
import org.example.mall_tiny01.mbg.mapper.OmsOrderMapper;
import org.example.mall_tiny01.mbg.model.*;
import org.example.mall_tiny01.service.CartItemService;
import org.example.mall_tiny01.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.config.ElasticsearchUtil;
import org.example.mall_tiny01.config.SnowflakeIdWorker;
import org.example.mall_tiny01.service.impl.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceimpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceimpl.class);

    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private UmsFeignClient umsFeignClient;

    @Autowired
    private OmsCartItemMapper omsCartItemMapper;

    @Autowired
    private PmsFeignClient pmsFeignClient;

    @Autowired
    private CacheQueryService cacheService;

    @Autowired
    private ElasticsearchUtil esUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient; // 注入 Redisson 分布式锁客户端

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker; // 注入雪花算法 ID 生成器，用于生成订单号

    @Autowired
    private MetricsService metricsService; // 注入监控埋点服务，用于记录下单耗时和成功率

    @Override
    public void cancelOrder(Long orderId) {
        OmsOrder order = omsOrderMapper.selectByPrimaryKey(orderId);
        // 只有待付款(0)的订单可以被系统取消
        if (order != null && order.getStatus() == 0) {
            order.setStatus(4); // 4 表示已关闭
            order.setModifyTime(new Date());
            omsOrderMapper.updateByPrimaryKeySelective(order);
        }
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeOutOrder() {
        log.info("开始执行定时任务：取消超时订单");

        List<OmsOrder> timeOutOrders = omsOrderMapper.selectTimeOutOrders(30);
        if (timeOutOrders != null && !timeOutOrders.isEmpty()) {
            List<Long> orderIds = new ArrayList<>();
            for (OmsOrder order : timeOutOrders) {
                orderIds.add(order.getId());
            }
            int count = omsOrderMapper.closeByIds(orderIds, "订单超时未支付，系统自动取消");
            log.info("定时任务执行完毕，共取消 {} 个超时订单", count);
        } else {
            log.info("没有超时订单需要处理");
        }
    }

    @Override
    public void cancelUserOrder(Long memberId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectByPrimaryKey(orderId);
        if (order != null && order.getMemberId().equals(memberId)) {
            // 只有待付款(0)和待发货(1)的订单可以被用户取消
            if (order.getStatus() == 0 || order.getStatus() == 1) {
                order.setStatus(4); // 4 表示已关闭
                order.setModifyTime(new Date());
                omsOrderMapper.updateByPrimaryKeySelective(order);
            } else {
                throw new RuntimeException("该订单状态无法取消");
            }
        } else {
            throw new RuntimeException("订单不存在或无权限");
        }
    }

    @Override
    public void confirmReceiveOrder(Long memberId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectByPrimaryKey(orderId);
        if (order != null && order.getMemberId().equals(memberId)) {
            // 只有已发货(2)的订单才能确认收货
            if (order.getStatus() == 2) {
                order.setStatus(3); // 3 表示已完成
                order.setReceiveTime(new Date());
                order.setModifyTime(new Date());
                omsOrderMapper.updateByPrimaryKeySelective(order);
            } else {
                throw new RuntimeException("该订单状态无法确认收货");
            }
        } else {
            throw new RuntimeException("订单不存在或无权限");
        }
    }

    @Override
    public void deleteOrder(Long memberId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectByPrimaryKey(orderId);
        if (order != null && order.getMemberId().equals(memberId)) {
            // 只有已完成(3)或已关闭(4)的订单才能被删除
            if (order.getStatus() == 3 || order.getStatus() == 4) {
                order.setDeleteStatus(1); // 1 表示已删除
                omsOrderMapper.updateByPrimaryKeySelective(order);
            } else {
                throw new RuntimeException("该订单状态无法删除");
            }
        } else {
            throw new RuntimeException("订单不存在或无权限");
        }
    }

    @Override
    public OmsOrderDetail getOrderDetail(Long memberId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectByPrimaryKey(orderId);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getMemberId().equals(memberId)) {
            throw new RuntimeException("无权限查看该订单");
        }
        
        OmsOrderDetail detail = new OmsOrderDetail();
        BeanUtils.copyProperties(order, detail);
        
        List<OmsOrderItem> orderItemList = omsOrderItemMapper.listByOrderId(orderId);
        detail.setOrderItemList(orderItemList);
        
        return detail;
    }

    @Override
    public ConfirmOrderResult generateConfirmOrder(Long memberId, List<Long> cartIds) {
        ConfirmOrderResult result = new ConfirmOrderResult();
        
        List<CartPromotionItem> cartPromotionItemList = cartItemService.listPromotion(memberId, cartIds);
        result.setCartPromotionItemList(cartPromotionItemList);
        
        List<UmsMemberReceiveAddress> addressList = umsFeignClient.listAddress(memberId).getData();
        result.setMemberReceiveAddressList(addressList);

        UmsMember member = umsFeignClient.getMemberById(memberId).getData();
        if (member != null) {
            result.setMemberId(memberId);
            result.setMemberNickname(member.getNickname());
            result.setMemberIntegration(member.getIntegration() == null ? 0 : member.getIntegration());
        }

        UmsIntegrationConsumeSetting integrationConsumeSetting = umsFeignClient.getIntegrationConsumeSetting(1L).getData();
        result.setIntegrationConsumeSetting(integrationConsumeSetting);
        
        CalcAmount calcAmount = calc(cartPromotionItemList);
        result.setCalcAmount(calcAmount);
        
        return result;
    }
    
    private CalcAmount calc(List<CartPromotionItem> cartPromotionItemList) {
        CalcAmount calcAmount = new CalcAmount();
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal promotionAmount = BigDecimal.ZERO;
        
        for (CartPromotionItem item : cartPromotionItemList) {
            BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemPrice);
            
            if (item.getReduceAmount() != null) {
                promotionAmount = promotionAmount.add(item.getReduceAmount());
            }
        }
        
        BigDecimal freightAmount = BigDecimal.ZERO;
        if (totalAmount.compareTo(new BigDecimal("99")) < 0) {
            freightAmount = new BigDecimal("10");
        }
        
        BigDecimal payAmount = totalAmount.subtract(promotionAmount).add(freightAmount);
        
        calcAmount.setTotalAmount(totalAmount);
        calcAmount.setFreightAmount(freightAmount);
        calcAmount.setPromotionAmount(promotionAmount);
        calcAmount.setPayAmount(payAmount);
        
        return calcAmount;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)  // 开启 Seata 全局事务
    public Long generateOrder(Long memberId, List<Long> cartIds, Long couponId, Long memberReceiveAddressId, Integer payType, Integer useIntegration, String orderUuid) {
        long startTime = System.currentTimeMillis(); // 记录开始时间，用于计算下单耗时

        // ==================== 步骤 0：幂等性校验（防重复提交） ====================
        String idempotentKey = "order:idempotent:" + orderUuid;
        
        // 利用 SETNX 尝试设置 Key，设置 5 分钟过期时间
        Boolean isFirstSubmit = stringRedisTemplate.opsForValue().setIfAbsent(idempotentKey, String.valueOf(memberId), 5, TimeUnit.MINUTES);
        
        if (Boolean.FALSE.equals(isFirstSubmit)) {
            // 如果 Key 已存在，说明是重复请求
            log.warn("检测到重复下单请求，orderUuid: {}, memberId: {}", orderUuid, memberId);
            metricsService.recordOrderFailed("idempotent", System.currentTimeMillis() - startTime);
            throw new RuntimeException("请勿重复提交订单");
        }

        try {
            // ==================== 步骤 1：校验基础数据 ====================
            
            // 1.1 校验用户是否存在
            UmsMember member = umsFeignClient.getMemberById(memberId).getData();
            if (member == null) {
                throw new RuntimeException("用户不存在");
            }

            // 1.2 获取购物车商品列表（包含促销信息）
            List<CartPromotionItem> cartPromotionItemList = cartItemService.listPromotion(memberId, cartIds);
            if (cartPromotionItemList == null || cartPromotionItemList.isEmpty()) {
                throw new RuntimeException("购物车为空");
            }

            // 1.3 校验收货地址是否存在
            UmsMemberReceiveAddress address = umsFeignClient.getAddressById(memberReceiveAddressId).getData();
            if (address == null) {
                throw new RuntimeException("收货地址不存在");
            }
            
            // 1.4 计算订单金额（商品总价、运费、优惠金额等）
            CalcAmount calcAmount = calc(cartPromotionItemList);
            
            // ==================== 步骤 2：组装订单主表数据 ====================
            
            OmsOrder order = new OmsOrder();
            order.setMemberId(memberId);
            order.setMemberUsername(member.getUsername());
            
            // 生成订单号（雪花算法，全局唯一且递增有序）
            String orderSn = String.valueOf(snowflakeIdWorker.nextId());
            order.setOrderSn(orderSn);
            
            order.setCreateTime(new Date());
            order.setStatus(0);          // 订单状态：0->待付款
            order.setOrderType(0);       // 订单类型：0->正常订单
            order.setSourceType(1);      // 订单来源：1->APP 订单
            order.setPayType(payType);   // 支付方式：0->支付宝，1->微信
            order.setUseIntegration(useIntegration == null ? 0 : useIntegration);  // 使用积分数量
            
            // ==================== 步骤 3：计算积分抵扣金额 ====================
            
            BigDecimal integrationAmount = BigDecimal.ZERO;
            if (useIntegration != null && useIntegration > 0) {
                // 获取积分抵扣规则（如：100 积分抵扣 1 元）
                UmsIntegrationConsumeSetting setting = umsFeignClient.getIntegrationConsumeSetting(1L).getData();
                if (setting != null && setting.getCouponStatus() == 1) {
                    // 计算可抵扣金额
                    BigDecimal perAmount = new BigDecimal(setting.getDeductionPerAmount());
                    integrationAmount = new BigDecimal(useIntegration).divide(perAmount, 2, BigDecimal.ROUND_DOWN);
                    // 抵扣金额不能超过应付金额
                    if (integrationAmount.compareTo(calcAmount.getPayAmount()) > 0) {
                        integrationAmount = calcAmount.getPayAmount();
                    }
                }
            }
            
            // 设置金额相关字段
            order.setTotalAmount(calcAmount.getTotalAmount());                  // 商品总价
            order.setPayAmount(calcAmount.getPayAmount().subtract(integrationAmount));  // 实际应付金额（减去积分抵扣）
            order.setFreightAmount(calcAmount.getFreightAmount());              // 运费
            order.setPromotionAmount(calcAmount.getPromotionAmount());          // 促销优惠金额
            order.setIntegrationAmount(integrationAmount);                      // 积分抵扣金额
            order.setCouponAmount(BigDecimal.ZERO);                             // 优惠券抵扣金额
            order.setDiscountAmount(BigDecimal.ZERO);                           // 折扣金额
            
            // 设置收货人信息
            order.setReceiverName(address.getName());
            order.setReceiverPhone(address.getPhoneNumber());
            order.setReceiverPostCode(address.getPostCode());
            order.setReceiverProvince(address.getProvince());
            order.setReceiverCity(address.getCity());
            order.setReceiverRegion(address.getRegion());
            order.setReceiverDetailAddress(address.getDetailAddress());
            
            // 设置其他默认字段
            order.setAutoConfirmDay(7);   // 7 天后自动确认收货
            order.setBillType(0);         // 票据类型：0->不开发票
            order.setConfirmStatus(0);    // 确认收货状态
            order.setDeleteStatus(0);     // 删除状态：0->未删除
            
            // ==================== 步骤 4：写入订单主表 ====================
            
            int count = omsOrderMapper.insertSelective(order);
            if (count <= 0) {
                throw new RuntimeException("订单创建失败");
            }
            
            // ==================== 步骤 5：写入订单明细表（每个商品一条记录） ====================
            
            for (CartPromotionItem cartItem : cartPromotionItemList) {
                OmsOrderItem orderItem = new OmsOrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setOrderSn(order.getOrderSn());
                
                // 商品基本信息
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setProductPic(cartItem.getProductPic());
                orderItem.setProductName(cartItem.getProductName());
                orderItem.setProductBrand(cartItem.getProductBrand());
                orderItem.setProductSn(cartItem.getProductSn());
                orderItem.setProductPrice(cartItem.getPrice());
                orderItem.setProductQuantity(cartItem.getQuantity());
                
                // SKU 信息
                orderItem.setProductSkuId(cartItem.getProductSkuId());
                orderItem.setProductSkuCode(cartItem.getProductSkuCode());
                orderItem.setProductCategoryId(cartItem.getProductCategoryId());
                
                // 金额信息
                orderItem.setPromotionAmount(cartItem.getReduceAmount() != null ? cartItem.getReduceAmount() : BigDecimal.ZERO);
                orderItem.setCouponAmount(BigDecimal.ZERO);
                orderItem.setIntegrationAmount(BigDecimal.ZERO);
                // 实际支付金额 = 单价 × 数量 - 促销优惠
                orderItem.setRealAmount(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())).subtract(orderItem.getPromotionAmount()));
                
                // 赠送积分和成长值
                orderItem.setGiftIntegration(cartItem.getIntegration() != null ? cartItem.getIntegration() : 0);
                orderItem.setGiftGrowth(cartItem.getGrowth() != null ? cartItem.getGrowth() : 0);
                orderItem.setProductAttr(cartItem.getProductAttr());
                
                // 插入订单明细
                omsOrderItemMapper.insertSelective(orderItem);
            }
            
            // ==================== 步骤 6：批量扣减库存（一条 SQL，乐观锁防超卖） ====================
            // 将购物车商品列表转换为批量扣减 DTO 列表
            List<SkuStockReduceDto> reduceList = cartPromotionItemList.stream()
                    .map(item -> new SkuStockReduceDto(item.getProductSkuId(), item.getQuantity()))
                    .collect(Collectors.toList());

            // 通过 Feign 远程调用 PMS 服务，一次性批量扣减所有 SKU 的库存
            // PMS 服务执行 CASE WHEN 批量 UPDATE，每个 SKU 带 stock >= quantity 乐观锁
            // 返回实际成功扣减的行数（全部成功则等于 reduceList.size()）
            Result<Integer> stockResult = pmsFeignClient.batchUpdateSkuStock(reduceList);
            int updateCount = stockResult.getData() != null ? stockResult.getData() : 0;

            if (updateCount != reduceList.size()) {
                // 有商品库存不足，Seata 全局事务自动回滚，订单不会生成
                throw new RuntimeException("部分商品库存不足，下单失败");
            }

            // ==================== 步骤 7：批量删除购物车中已下单的商品 ====================
            // 使用批量删除替代循环单条删除，减少数据库交互次数
            if (cartIds != null && !cartIds.isEmpty()) {
                omsCartItemMapper.deleteBatch(cartIds);
            }
            
            // ==================== 步骤 8：注册事务后回调（ES 和 Redis 同步） ====================
            // 核心业务已完成，将辅助数据同步注册为事务提交后的回调任务。
            // 这样 ES/Redis 操作就不在 Seata 全局事务范围内，即使失败也不回滚订单。
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 事务提交成功后才执行这里的代码
                        try {
                            for (CartPromotionItem cartItem : cartPromotionItemList) {
                                Long productId = cartItem.getProductId();
                                
                                // 1. 清理商品详情缓存
                                cacheService.invalidate("pms:product:" + productId);
                                log.info("已清理商品 {} 的 Redis 缓存", productId);
                                
                                // 2. 同步商品到 ES（更新销量、库存等）
                                syncProductToEs(productId);
                            }
                        } catch (Exception e) {
                            // 辅助数据同步失败只记录日志，不影响订单生成
                            log.error("订单生成成功，但 ES/Redis 同步失败，稍后由定时任务补偿", e);
                        }
                    }
                });
            }

            // ==================== 步骤 9：返回订单 ID ====================
            // 前端拿到这个 ID 后，会跳转到支付页面
            metricsService.recordOrderSuccess(System.currentTimeMillis() - startTime);
            return order.getId();

        } catch (RuntimeException e) {
            // 如果下单失败（如库存不足、用户不存在等），记录监控指标后删除幂等 Key
            metricsService.recordOrderFailed("stock_insufficient", System.currentTimeMillis() - startTime);
            stringRedisTemplate.delete(idempotentKey);
            throw e;
        } catch (Exception e) {
            // 捕获其他未知异常，记录监控指标后删除幂等 Key
            metricsService.recordOrderFailed("unknown", System.currentTimeMillis() - startTime);
            stringRedisTemplate.delete(idempotentKey);
            log.error("下单过程发生未知异常", e);
            throw new RuntimeException("下单失败，请稍后重试");
        }
    }
    
    /**
     * 同步商品数据到 ES（订单成功后更新销量）
     */
    private void syncProductToEs(Long productId) {
        try {
            // 通过 Feign 获取商品最新信息（包含更新后的销量）
            Result<PmsProduct> productResult = pmsFeignClient.getProduct(productId);
            
            if (productResult != null && productResult.getData() != null) {
                PmsProduct product = productResult.getData();
                
                // 将商品同步到 ES 索引
                // 索引名: pms_product，文档 ID: 商品 ID
                esUtil.save("pms_product", String.valueOf(productId), product);
                
                log.info("商品 {} 已同步到 ES，销量: {}", productId, product.getSale());
            } else {
                log.warn("同步商品 {} 到 ES 失败：商品不存在", productId);
            }
        } catch (Exception e) {
            log.error("同步商品 {} 到 ES 异常", productId, e);
        }
    }

    @Override
    public PageResult<OmsOrderDetail> list(Long memberId, Integer status, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<OmsOrder> orderList = omsOrderMapper.list(null, status, null, null, memberId, null, null);

        List<OmsOrderDetail> detailList = new ArrayList<>();
        for (OmsOrder order : orderList) {
            OmsOrderDetail detail = new OmsOrderDetail();
            BeanUtils.copyProperties(order, detail);
            
            List<OmsOrderItem> orderItemList = omsOrderItemMapper.listByOrderId(order.getId());
            detail.setOrderItemList(orderItemList);
            
            detailList.add(detail);
        }
        
        PageInfo<OmsOrder> pageInfo = new PageInfo<>(orderList);
        
        PageResult<OmsOrderDetail> pageResult = new PageResult<>();
        pageResult.setList(detailList);
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setTotalPage(pageInfo.getPages());
        
        return pageResult;
    }

    @Override
    public void paySuccess(Long orderId, Integer payType) {
        OmsOrder order = omsOrderMapper.selectByPrimaryKey(orderId);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(1); // 1->待发货
        order.setPayType(payType);
        order.setPaymentTime(new Date());
        order.setModifyTime(new Date());
        
        omsOrderMapper.updateByPrimaryKeySelective(order);
    }
}