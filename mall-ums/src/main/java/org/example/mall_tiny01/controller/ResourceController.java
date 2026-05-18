package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.UmsResource;
import org.example.mall_tiny01.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resource")
public class ResourceController {
    @Autowired
    private ResourceService resourceService;

    @PostMapping("/create")
    @ApiOperation("添加后台资源")
    public Result createResource(@RequestBody UmsResource resource) {
        resourceService.createResource(resource);
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("根据ID删除后台资源")
    public Result deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("分页模糊查询后台资源")
    public Result<PageResult<UmsResource>> list(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "nameKeyword", required = false) String nameKeyword,
            @RequestParam(value = "urlKeyword", required = false) String urlKeyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        PageResult<UmsResource> pageResult = resourceService.list(categoryId, nameKeyword, urlKeyword, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/listAll")
    @ApiOperation("查询所有后台资源")
    public Result<List<UmsResource>> listAll() {
        List<UmsResource> list = resourceService.listAll();
        return Result.success(list);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改后台资源")
    public Result updateResource(@PathVariable Long id, @RequestBody UmsResource resource) {
        resourceService.updateResource(id, resource);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取资源详情")
    public Result<UmsResource> getResource(@PathVariable Long id) {
        UmsResource resource = resourceService.getResource(id);
        return Result.success(resource);
    }
}