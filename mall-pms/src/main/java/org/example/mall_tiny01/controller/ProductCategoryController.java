package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductCategoryParam;
import org.example.mall_tiny01.dto.PmsProductCategoryWithChildrenItem;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.PmsProductCategory;
import org.example.mall_tiny01.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productCategory")
public class ProductCategoryController {
    @Autowired
    private ProductCategoryService productCategoryService;

    @PostMapping("/create")
    @ApiOperation("添加商品分类")
    public Result createCategory(@RequestBody PmsProductCategoryParam productCategoryParam) {
        productCategoryService.createCategory(productCategoryParam);
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("根据ID删除商品分类")
    public Result deleteCategory(@PathVariable Long id) {
        productCategoryService.deleteCategory(id);
        return Result.success();
    }

    @GetMapping("/list/withChildren")
    @ApiOperation("查询所有一级分类及子分类")
    public Result<List<PmsProductCategoryWithChildrenItem>> listWithChildren() {
        List<PmsProductCategoryWithChildrenItem> list = productCategoryService.listWithChildren();
        return Result.success(list);
    }

    @GetMapping("/list/{parentId}")
    @ApiOperation("分页查询商品分类")
    public Result<PageResult<PmsProductCategory>> listByParentId(
            @PathVariable Long parentId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        PageResult<PmsProductCategory> pageResult = productCategoryService.listByParentId(parentId, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @PostMapping("/update/navStatus")
    @ApiOperation("批量修改导航栏显示状态")
    public Result updateNavStatus(@RequestParam List<Long> ids, @RequestParam Integer navStatus) {
        productCategoryService.updateNavStatus(ids, navStatus);
        return Result.success();
    }

    @PostMapping("/update/showStatus")
    @ApiOperation("批量修改显示状态")
    public Result updateShowStatus(@RequestParam List<Long> ids, @RequestParam Integer showStatus) {
        productCategoryService.updateShowStatus(ids, showStatus);
        return Result.success();
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改商品分类")
    public Result updateCategory(@PathVariable Long id, @RequestBody PmsProductCategoryParam productCategoryParam) {
        productCategoryService.updateCategory(id, productCategoryParam);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取商品分类")
    public Result<PmsProductCategory> getCategory(@PathVariable Long id) {
        PmsProductCategory category = productCategoryService.getCategory(id);
        return Result.success(category);
    }
}