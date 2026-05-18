package org.example.mall_tiny01.dto;

import lombok.Data;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.mbg.model.PmsProductAttribute;
import org.example.mall_tiny01.mbg.model.PmsSkuStock;

import java.util.List;

@Data
public class CartProduct extends PmsProduct {
    private List<PmsSkuStock> skuStockList;
    private List<PmsProductAttribute> productAttributeList;
}