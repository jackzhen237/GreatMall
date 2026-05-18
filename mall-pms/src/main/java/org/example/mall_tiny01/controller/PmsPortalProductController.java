package org.example.mall_tiny01.controller;

import org.example.mall_tiny01.dto.CartProduct;
import org.example.mall_tiny01.dto.PmsPortalProductDetail;
import org.example.mall_tiny01.dto.PmsProductCategoryNode;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.mapper.PmsProductMapper;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.service.PmsPortalProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class PmsPortalProductController {

    @Autowired
    private PmsPortalProductService portalProductService;

    @Autowired
    private PmsProductMapper productMapper;

    @GetMapping("/categoryTreeList")
    public Result<List<PmsProductCategoryNode>> categoryTreeList() {
        List<PmsProductCategoryNode> treeList = portalProductService.categoryTreeList();
        return Result.success(treeList);
    }

    @GetMapping("/detail/{id}")
    public Result<PmsPortalProductDetail> detail(@PathVariable Long id) {
        PmsPortalProductDetail detail = portalProductService.detail(id);
        if (detail != null) {
            return Result.success(detail);
        }
        return Result.error("商品不存在");
    }

    @GetMapping("/{id}")
    public Result<PmsProduct> getProduct(@PathVariable Long id) {
        PmsProduct product = productMapper.selectByPrimaryKey(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    @GetMapping("/cartProduct/{productId}")
    public Result<CartProduct> getCartProduct(@PathVariable Long productId) {
        CartProduct cartProduct = productMapper.getCartProduct(productId);
        if (cartProduct == null) {
            return Result.error("商品不存在");
        }
        return Result.success(cartProduct);
    }
}
