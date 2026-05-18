package org.example.mall_tiny01.service.impl;

import com.alibaba.fastjson.JSON;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.config.ElasticsearchUtil;
import org.example.mall_tiny01.mbg.mapper.PmsSkuStockMapper;
import org.example.mall_tiny01.mbg.model.PmsSkuStock;
import org.example.mall_tiny01.service.SkuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.seata.spring.annotation.GlobalTransactional;

import java.util.List;

@Service
public class SkuServiceimpl implements SkuService {

    private static final Logger log = LoggerFactory.getLogger(SkuServiceimpl.class);

    @Autowired
    private PmsSkuStockMapper pmsSkuStockMapper;

    @Autowired
    private CacheQueryService cacheService;

    @Autowired
    private ElasticsearchUtil esUtil;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateStock(Long productId, List<PmsSkuStock> skuStockList) {
        // 1. 更新 MySQL：先删后插
        pmsSkuStockMapper.deleteByProductId(productId);

        if (skuStockList != null && !skuStockList.isEmpty()) {
            for (PmsSkuStock skuStock : skuStockList) {
                skuStock.setProductId(productId);
                skuStock.setId(null);
                pmsSkuStockMapper.insertSelective(skuStock);
            }
        }

        // 2. 同步 ES（全量替换该商品下的 SKU）
        syncSkuToEs(productId);

        // 3. 删除缓存
        cacheService.invalidate("pms:sku:list:" + productId + ":*");
    }

    @Override
    public List<PmsSkuStock> list(Long productId, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isEmpty();

        if (hasKeyword) {
            // 搜索链路：ES 拼音分词搜索替代 LIKE
            String cacheKey = "search:pms:sku:" + keyword
                    + ":product:" + (productId != null ? productId : "all");

            String cached = cacheService.query(cacheKey, String.class, () -> {
                try {
                    List<PmsSkuStock> esResults;
                    if (productId != null) {
                        // 先搜索 ES，再按 productId 过滤
                        esResults = esUtil.search("pms_sku_stock", "skuCode", keyword, PmsSkuStock.class);
                        esResults = esResults.stream()
                                .filter(s -> productId.equals(s.getProductId()))
                                .collect(java.util.stream.Collectors.toList());
                    } else {
                        esResults = esUtil.search("pms_sku_stock", "skuCode", keyword, PmsSkuStock.class);
                    }
                    return JSON.toJSONString(esResults);
                } catch (Exception e) {
                    log.error("ES SKU 搜索失败，降级到 MySQL. keyword={}", keyword, e);
                    List<PmsSkuStock> list = pmsSkuStockMapper.list(productId, keyword);
                    return JSON.toJSONString(list);
                }
            });

            if (cached == null || cached.isEmpty()) return null;
            return JSON.parseArray(cached, PmsSkuStock.class);
        }

        // 非搜索链路：MySQL 直查 + 缓存
        String cacheKey = "pms:sku:list:" + (productId != null ? productId : "all");

        String cached = cacheService.query(cacheKey, String.class, () -> {
            List<PmsSkuStock> list = pmsSkuStockMapper.list(productId, null);
            return JSON.toJSONString(list);
        });

        if (cached == null || cached.isEmpty()) return null;
        return JSON.parseArray(cached, PmsSkuStock.class);
    }

    // ======================== ES 同步辅助方法 ========================

    private void syncSkuToEs(Long productId) {
        try {
            // 先删除该 productId 下所有旧 SKU 的 ES 文档
            List<PmsSkuStock> allSkus = pmsSkuStockMapper.list(productId, null);
            if (allSkus != null) {
                for (PmsSkuStock sku : allSkus) {
                    esUtil.save("pms_sku_stock", String.valueOf(sku.getId()), sku);
                }
            }
        } catch (Exception e) {
            log.error("ES SKU 同步失败（不阻断主流程）. productId={}", productId, e);
        }
    }
}
