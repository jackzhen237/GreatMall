package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.dto.UmsMenuNode;
import org.example.mall_tiny01.mbg.model.UmsMenu;
import org.example.mall_tiny01.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/menu")
@ApiOperation("菜单管理")
public class MenuController {
    @Autowired
    private MenuService menuService;

    @PostMapping("/create")
    @ApiOperation("添加后台菜单")
    public Result createMenu(@RequestBody UmsMenu menu) {
        menuService.createMenu(menu);
        return Result.success("操作成功");
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("根据ID删除后台菜单")
    public Result deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success("操作成功");
    }

    @GetMapping("/list/{parentId}")
    @ApiOperation("根据上级菜单ID分页查询菜单")
    public Result list(
            @PathVariable Long parentId,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 5;
        }
        
        PageResult<UmsMenu> list = menuService.list(parentId, pageNum, pageSize);
        return Result.success(list);
    }

    @GetMapping("/treeList")
    @ApiOperation("树形结构返回所有菜单列表")
    public Result treeList() {
        List<UmsMenuNode> treeList = menuService.treeList();
        return Result.success(treeList);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改后台菜单")
    public Result updateMenu(@PathVariable Long id, @RequestBody UmsMenu menu) {
        menuService.updateMenu(id, menu);
        return Result.success("操作成功");
    }

    @PostMapping("/updateHidden/{id}")
    @ApiOperation("根据ID修改菜单显示状态")
    public Result updateHidden(@PathVariable Long id, @RequestParam Integer hidden) {
        menuService.updateHidden(id, hidden);
        return Result.success("操作成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取菜单详情")
    public Result getById(@PathVariable Long id) {
        UmsMenu menu = menuService.getById(id);
        return Result.success(menu);
    }
}
