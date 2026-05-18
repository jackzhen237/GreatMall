package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductAttributeCategoryItem;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.PmsProductAttribute;
import org.example.mall_tiny01.mbg.model.PmsProductAttributeCategory;
import org.example.mall_tiny01.service.ProductAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productAttribute")
public class ProductAttributeController {
    @Autowired
    private ProductAttributeService productAttributeService;

    @PostMapping("/create")
    @ApiOperation("添加商品属性")
    public Result createAttribute(@RequestBody PmsProductAttribute productAttribute) {
        productAttributeService.createAttribute(productAttribute);
        return Result.success();
    }

    @PostMapping("/delete")
    @ApiOperation("删除单个/批量商品属性")
    public Result deleteAttribute(@RequestParam List<Long> ids) {
        productAttributeService.deleteAttribute(ids);
        return Result.success();
    }

    @GetMapping("/list/{cid}")
    @ApiOperation("根据分类ID查询属性列表或参数列表")
    public Result<PageResult<PmsProductAttribute>> listAttribute(
            @PathVariable Long cid,
            @RequestParam Integer type,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        PageResult<PmsProductAttribute> pageResult = productAttributeService.listAttribute(cid, type, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/list/withAttr")
    @ApiOperation("获取所有商品属性分类及其下属")
    public Result<List<PmsProductAttributeCategoryItem>> listWithAttr() {
        List<PmsProductAttributeCategoryItem> list = productAttributeService.listWithAttr();
        return Result.success(list);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改商品属性")
    public Result updateAttribute(@PathVariable Long id, @RequestBody PmsProductAttribute productAttribute) {
        productAttribute.setId(id);
        productAttributeService.updateAttribute(productAttribute);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询商品属性")
    public Result<PmsProductAttribute> getAttribute(@PathVariable Long id) {
        PmsProductAttribute attribute = productAttributeService.getAttribute(id);
        return Result.success(attribute);
    }

    @GetMapping("/attrInfo/{productCategoryId}")
    @ApiOperation("根据商品分类的ID获取商品属性及属性分类ID")
    public Result<PmsProductAttributeCategory> getAttrInfo(@PathVariable Long productCategoryId) {
        PmsProductAttributeCategory category = productAttributeService.getCategory(productCategoryId);
        return Result.success(category);
    }
}