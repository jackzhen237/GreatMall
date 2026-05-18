package org.example.mall_tiny01.dto;

/**
 * 批量扣减库存 DTO —— 一次请求扣减多个 SKU，替代串行逐个调用
 */
public class SkuStockReduceDto {
    /** 商品 SKU ID */
    private Long skuId;
    /** 扣减数量 */
    private Integer quantity;

    public SkuStockReduceDto() {}

    public SkuStockReduceDto(Long skuId, Integer quantity) {
        this.skuId = skuId;
        this.quantity = quantity;
    }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
