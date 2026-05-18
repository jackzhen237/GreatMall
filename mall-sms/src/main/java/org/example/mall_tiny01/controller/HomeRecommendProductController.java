package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.SmsHomeRecommendProduct;
import org.example.mall_tiny01.service.HomeRecommendProductService;
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
@RequestMapping("/home/recommendProduct")
@ApiOperation("首页人气推荐管理")
public class HomeRecommendProductController {
    @Autowired
    private HomeRecommendProductService homeRecommendProductService;

    @PostMapping("/create")
    @ApiOperation("批量添加首页推荐")
    public Result createHomeRecommendProduct(@RequestBody List<SmsHomeRecommendProduct> recommendProductList) {
        homeRecommendProductService.createHomeRecommendProduct(recommendProductList);
        return Result.success("操作成功");
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除推荐")
    public Result deleteHomeRecommendProduct(@RequestParam("ids") Long[] ids) {
        homeRecommendProductService.deleteHomeRecommendProductBatch(ids);
        return Result.success("操作成功");
    }

    @GetMapping("/list")
    @ApiOperation("分页查询推荐")
    public Result list(
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "recommendStatus", required = false) Integer recommendStatus,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 5;
        }
        
        PageResult<SmsHomeRecommendProduct> list = homeRecommendProductService.list(productName, recommendStatus, pageNum, pageSize);
        return Result.success(list);
    }

    @PostMapping("/update/recommendStatus")
    @ApiOperation("批量修改推荐状态")
    public Result updateRecommendStatus(
            @RequestParam("ids") Long[] ids,
            @RequestParam("recommendStatus") Integer recommendStatus) {
        homeRecommendProductService.updateRecommendStatus(ids, recommendStatus);
        return Result.success("操作成功");
    }

    @PostMapping("/update/sort/{id}")
    @ApiOperation("修改推荐排序")
    public Result updateSort(@PathVariable Long id, @RequestParam(value = "sort", required = false) Integer sort) {
        homeRecommendProductService.updateSort(id, sort);
        return Result.success("操作成功");
    }
}
