package org.example.mall_tiny01.feign;

import org.example.mall_tiny01.dto.CartProduct;
import org.example.mall_tiny01.dto.PageResult;
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

    /**
     * 获取所有品牌列表（用于 RAG 知识库初始化）
     */
    @GetMapping("/brand/listAll")
    Result<List<PmsBrand>> listAllBrands();

    /**
     * 分页获取商品列表（用于 RAG 知识库初始化）
     */
    @GetMapping("/product/list")
    Result<PageResult<PmsProduct>> listProducts(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize
    );

    /**
     * 获取推荐品牌列表（用于 RAG 知识库初始化）
     */
    @GetMapping("/brand/recommendList")
    Result<PageResult<PmsBrand>> getRecommendBrands(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize
    );

}
