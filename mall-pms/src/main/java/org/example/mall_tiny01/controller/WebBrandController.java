package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.service.WebBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brand")
public class WebBrandController {
    @Autowired
    private WebBrandService webBrandService;

    @GetMapping("/detail/{brandId}")
    @ApiOperation("获取品牌详情")
    public Result<PmsBrand> detail(@PathVariable Long brandId) {
        PmsBrand brand = webBrandService.getDetail(brandId);
        return Result.success(brand);
    }

    @GetMapping("/productList")
    @ApiOperation("分页获取品牌相关商品")
    public Result<PageResult<PmsProduct>> productList(
            @RequestParam Long brandId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        PageResult<PmsProduct> pageResult = webBrandService.getProductList(brandId, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/recommendList")
    @ApiOperation("分页获取推荐品牌")
    public Result<PageResult<PmsBrand>> recommendList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        PageResult<PmsBrand> pageResult = webBrandService.getRecommendList(pageNum, pageSize);
        return Result.success(pageResult);
    }
}