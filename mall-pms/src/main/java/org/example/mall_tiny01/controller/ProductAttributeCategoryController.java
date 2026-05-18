package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductAttributeCategoryItem;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.PmsProductAttributeCategory;
import org.example.mall_tiny01.service.ProductAttributeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productAttribute/category")
public class ProductAttributeCategoryController {
    @Autowired
    private ProductAttributeCategoryService productAttributeCategoryService;

    @PostMapping("/create")
    @ApiOperation("添加商品属性分类")
    public Result createCategory(@RequestParam String name) {
        PmsProductAttributeCategory category = new PmsProductAttributeCategory();
        category.setName(name);
        productAttributeCategoryService.createCategory(category);
        return Result.success();
    }

    @GetMapping("/delete/{id}")
    @ApiOperation("删除单个商品属性分类")
    public Result deleteCategory(@PathVariable Long id) {
        productAttributeCategoryService.deleteCategory(id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("分页获取所有商品属性分类")
    public Result<PageResult<PmsProductAttributeCategory>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        PageResult<PmsProductAttributeCategory> pageResult = productAttributeCategoryService.list(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/list/withAttr")
    @ApiOperation("获取所有商品属性分类及其下属属性")
    public Result<List<PmsProductAttributeCategoryItem>> listWithAttr() {
        List<PmsProductAttributeCategoryItem> list = productAttributeCategoryService.listWithAttr();
        return Result.success(list);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改商品属性分类")
    public Result updateCategory(@PathVariable Long id, @RequestParam String name) {
        productAttributeCategoryService.updateCategory(id, name);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("获取单个商品属性分类信息")
    public Result<PmsProductAttributeCategory> getCategory(@PathVariable Long id) {
        PmsProductAttributeCategory category = productAttributeCategoryService.getCategory(id);
        return Result.success(category);
    }
}
