package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.mall_tiny01.dto.HomeContentResult;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.CmsSubject;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.mbg.model.PmsProductCategory;
import org.example.mall_tiny01.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home")
public class
HomeController {
    @Autowired
    private HomeService homeService;

    @GetMapping("/content")
    @ApiOperation("首页内容信息展示")
    public Result<HomeContentResult> content() {
        HomeContentResult result = homeService.content();
        return Result.success(result);
    }

    @GetMapping("/hotProductList")
    @ApiOperation("分页获取人气推荐商品")
    public Result<PageResult<PmsProduct>> hotProductList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        PageResult<PmsProduct> result = homeService.getHotProductList(pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/newProductList")
    @ApiOperation("分页获取新品推荐商品")
    public Result<PageResult<PmsProduct>> newProductList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        PageResult<PmsProduct> result = homeService.getNewProductList(pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/recommendProductList")
    @ApiOperation("分页获取推荐商品")
    public Result<PageResult<PmsProduct>> recommendProductList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize) {
        PageResult<PmsProduct> result = homeService.getRecommendProductList(pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/productCateList/{parentId}")
    @ApiOperation("获取首页商品分类")
    public Result<List<PmsProductCategory>> getProductCateList(
            @ApiParam("父级分类ID") @PathVariable Long parentId) {
        List<PmsProductCategory> list = homeService.getProductCateList(parentId);
        return Result.success(list);
    }

    @GetMapping("/subjectList")
    @ApiOperation("根据分类分页获取专题")
    public Result<PageResult<CmsSubject>> getSubjectList(
            @RequestParam(value = "cateId", required = false) Long cateId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize) {
        PageResult<CmsSubject> result = homeService.getSubjectList(cateId, pageNum, pageSize);
        return Result.success(result);
    }
}
