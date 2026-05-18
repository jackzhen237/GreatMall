package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.UmsMenu;
import org.example.mall_tiny01.mbg.model.UmsResource;
import org.example.mall_tiny01.mbg.model.UmsRole;
import org.example.mall_tiny01.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @PostMapping("/create")
    @ApiOperation("添加角色")
    public Result createRole(@RequestBody UmsRole role) {
        roleService.createRole(role);
        return Result.success();
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除角色")
    public Result deleteRole(@RequestParam List<Long> ids) {
        roleService.deleteRole(ids);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据角色名称分页获取角色列表")
    public Result<PageResult<UmsRole>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        PageResult<UmsRole> pageResult = roleService.list(keyword, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/listAll")
    @ApiOperation("获取所有角色")
    public Result<List<UmsRole>> listAll() {
        List<UmsRole> list = roleService.listAll();
        return Result.success(list);
    }

    @PostMapping("/allocMenu")
    @ApiOperation("给角色分配菜单")
    public Result allocMenu(@RequestParam Long roleId, @RequestParam List<Long> menuIds) {
        roleService.allocMenu(roleId, menuIds);
        return Result.success();
    }

    @PostMapping("/allocResource")
    @ApiOperation("给角色分配资源")
    public Result allocResource(@RequestParam Long roleId, @RequestParam List<Long> resourceIds) {
        roleService.allocResource(roleId, resourceIds);
        return Result.success();
    }

    @GetMapping("/listMenu/{roleId}")
    @ApiOperation("根据角色ID获取菜单列表")
    public Result<List<UmsMenu>> listMenu(@PathVariable Long roleId) {
        List<UmsMenu> list = roleService.listMenu(roleId);
        return Result.success(list);
    }

    @GetMapping("/listResource/{roleId}")
    @ApiOperation("获取角色相关资源")
    public Result<List<UmsResource>> listResource(@PathVariable Long roleId) {
        List<UmsResource> list = roleService.listResource(roleId);
        return Result.success(list);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改角色")
    public Result updateRole(@PathVariable Long id, @RequestBody UmsRole role) {
        roleService.updateRole(id, role);
        return Result.success();
    }

    @PostMapping("/updateStatus/{id}")
    @ApiOperation("修改角色状态")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        roleService.updateStatus(id, status);
        return Result.success();
    }
}
