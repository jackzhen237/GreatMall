package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.PmsSkuStock;

import java.util.List;

public interface SkuService {
    void updateStock(Long productId, List<PmsSkuStock> skuStockList);

    List<PmsSkuStock> list(Long productId, String keyword);
}
