package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.BrandParam;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @PostMapping("/create")
    @ApiOperation("创建品牌")
    public Result createBrand(@RequestBody BrandParam brandParam) {
        brandService.createBrand(brandParam);
        return Result.success();
    }

    @PostMapping("/update/{id}")
    @ApiOperation("更新品牌")
    public Result update(@PathVariable Long id, @RequestBody BrandParam brandParam) {
        brandService.update(id, brandParam);
        return Result.success();
    }

    @PostMapping("/update/factoryStatus")
    @ApiOperation("批量更新厂家制造商状态")
    public Result updateFactoryStatus(@RequestParam("ids") Long[] ids, @RequestParam("factoryStatus") Integer factoryStatus) {
        brandService.updateFactoryStatus(ids, factoryStatus);
        return Result.success();
    }

    @PostMapping("/update/showStatus")
    @ApiOperation("批量更新显示状态")
    public Result updateShowStatus(@RequestParam("ids") Long[] ids, @RequestParam("showStatus") Integer showStatus) {
        brandService.updateShowStatus(ids, showStatus);
        return Result.success();
    }

    @PostMapping("/delete/batch")
    @ApiOperation("批量删除品牌")
    public Result deleteBrand(@RequestBody Integer[] ids) {
        brandService.deleteBrand(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据编号查询品牌信息")
    public Result getById(@PathVariable Long id) {
        PmsBrand pmsBrand = brandService.getById(id);
        return Result.success(pmsBrand);
    }

    @GetMapping("/delete/{id}")
    @ApiOperation("删除指定品牌")
    public Result deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据 keyword 名称分页获取品牌列表")
    public Result list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "showStatus", required = false) Integer showStatus) {

        PageResult<PmsBrand> list = brandService.list(keyword, pageNum, pageSize, showStatus);
        return Result.success(list);
    }

    @GetMapping("/listAll")
    @ApiOperation("获取所有品牌")
    public Result listAll() {
        List<PmsBrand> list = brandService.listAll();
        return Result.success(list);
    }
}
