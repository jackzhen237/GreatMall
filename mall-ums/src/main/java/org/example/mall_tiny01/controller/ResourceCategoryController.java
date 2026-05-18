package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.UmsResourceCategory;
import org.example.mall_tiny01.service.ResourceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resourceCategory")
public class ResourceCategoryController {
    @Autowired
    private ResourceCategoryService resourceCategoryService;

    @PostMapping("/create")
    @ApiOperation("添加后台资源分类")
    public Result createCategory(@RequestBody UmsResourceCategory category) {
        resourceCategoryService.createCategory(category);
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("根据ID删除后台资源分类")
    public Result deleteCategory(@PathVariable Long id) {
        resourceCategoryService.deleteCategory(id);
        return Result.success();
    }

    @GetMapping("/listAll")
    @ApiOperation("查询所有后台资源分类")
    public Result<List<UmsResourceCategory>> listAll() {
        List<UmsResourceCategory> list = resourceCategoryService.listAll();
        return Result.success(list);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改后台资源分类")
    public Result updateCategory(@PathVariable Long id, @RequestBody UmsResourceCategory category) {
        category.setId(id);
        resourceCategoryService.updateCategory(id, category);
        return Result.success();
    }
}