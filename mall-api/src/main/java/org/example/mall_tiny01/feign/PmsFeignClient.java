package org.example.mall_tiny01.feign;

import org.example.mall_tiny01.dto.CartProduct;
import org.example.mall_tiny01.dto.PmsPortalProductDetail;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.dto.SkuStockReduceDto;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-pms")
public interface PmsFeignClient {

    @GetMapping("/brand/detail/{brandId}")
    Result<PmsBrand> getBrandDetail(@PathVariable("brandId") Long brandId);

    @GetMapping("/product/detail/{id}")
    Result<PmsPortalProductDetail> getProductDetail(@PathVariable("id") Long id);

    @GetMapping("/product/{id}")
    Result<PmsProduct> getProduct(@PathVariable("id") Long id);

    @GetMapping("/product/cartProduct/{productId}")
    Result<CartProduct> getCartProduct(@PathVariable("productId") Long productId);

    @PostMapping("/sku/stock/deduct")
    Result<Integer> updateSkuStock(@RequestParam("skuId") Long skuId,
                                   @RequestParam("quantity") Integer quantity);

    @PostMapping("/sku/stock/batchReduce")
    Result<Integer> batchUpdateSkuStock(@RequestBody List<SkuStockReduceDto> reduceList);

}
