package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.SmsHomeNewProduct;
import org.example.mall_tiny01.service.HomeNewproductService;
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
@RequestMapping("/home/newProduct")
@ApiOperation("首页新品推荐管理")
public class HomeNewproductController {
    @Autowired
    private HomeNewproductService homeNewproductService;

    @PostMapping("/create")
    @ApiOperation("批量添加首页新品")
    public Result createHomeNewProduct(@RequestBody List<SmsHomeNewProduct> newProductList) {
        homeNewproductService.createHomeNewProduct(newProductList);
        return Result.success("操作成功");
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除首页新品")
    public Result deleteHomeNewProduct(@RequestParam("ids") Long[] ids) {
        homeNewproductService.deleteHomeNewProductBatch(ids);
        return Result.success("操作成功");
    }

    @GetMapping("/list")
    @ApiOperation("分页查询首页新品")
    public Result list(
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "recommendStatus", required = false) Integer recommendStatus,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 5;
        }
        
        PageResult<SmsHomeNewProduct> list = homeNewproductService.list(productName, recommendStatus, pageNum, pageSize);
        return Result.success(list);
    }

    @PostMapping("/update/recommendStatus")
    @ApiOperation("批量修改首页新品状态")
    public Result updateRecommendStatus(
            @RequestParam("ids") Long[] ids,
            @RequestParam("recommendStatus") Integer recommendStatus) {
        homeNewproductService.updateRecommendStatus(ids, recommendStatus);
        return Result.success("操作成功");
    }

    @PostMapping("/update/sort/{id}")
    @ApiOperation("修改首页新品排序")
    public Result updateSort(@PathVariable Long id, @RequestParam(value = "sort", required = false) Integer sort) {
        homeNewproductService.updateSort(id, sort);
        return Result.success("操作成功");
    }
}
