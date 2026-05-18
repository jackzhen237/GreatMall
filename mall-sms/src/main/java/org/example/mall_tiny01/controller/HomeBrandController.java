package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.SmsHomeBrand;
import org.example.mall_tiny01.service.HomeBrandService;
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
@RequestMapping("/home/brand")
@ApiOperation("首页品牌推荐管理")
public class HomeBrandController {
    @Autowired
    private HomeBrandService homeBrandService;

    @PostMapping("/create")
    @ApiOperation("添加首页推荐品牌")
    public Result createHomeBrand(@RequestBody List<SmsHomeBrand> homeBrandList) {
        homeBrandService.createHomeBrand(homeBrandList);
        return Result.success("操作成功");
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除推荐品牌")
    public Result deleteHomeBrand(@RequestParam("ids") Long[] ids) {
        homeBrandService.deleteHomeBrandBatch(ids);
        return Result.success("操作成功");
    }

    @GetMapping("/list")
    @ApiOperation("分页查询推荐品牌")
    public Result list(
            @RequestParam(value = "brandName", required = false) String brandName,
            @RequestParam(value = "recommendStatus", required = false) Integer recommendStatus,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 5;
        }
        
        PageResult<SmsHomeBrand> list = homeBrandService.list(brandName, recommendStatus, pageNum, pageSize);
        return Result.success(list);
    }

    @PostMapping("/update/recommendStatus")
    @ApiOperation("批量修改推荐品牌状态")
    public Result updateRecommendStatus(
            @RequestParam("ids") Long[] ids,
            @RequestParam("recommendStatus") Integer recommendStatus) {
        homeBrandService.updateRecommendStatus(ids, recommendStatus);
        return Result.success("操作成功");
    }

    @PostMapping("/update/sort/{id}")
    @ApiOperation("修改推荐品牌排序")
    public Result updateSort(@PathVariable Long id, @RequestParam(value = "sort", required = false) Integer sort) {
        homeBrandService.updateSort(id, sort);
        return Result.success("操作成功");
    }
}
