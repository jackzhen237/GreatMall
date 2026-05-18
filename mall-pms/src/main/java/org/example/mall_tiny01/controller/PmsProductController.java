package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductParam;
import org.example.mall_tiny01.dto.PmsProductResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.service.PmsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class PmsProductController {
    @Autowired
    private PmsProductService pmsProductService;

    @PostMapping("/create")
    @ApiOperation("创建商品")
    public Result createProduct(@RequestBody PmsProductParam productParam) {
        pmsProductService.createProduct(productParam);
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation("根据条件分页查询商品列表")
    public Result<PageResult<PmsProduct>> list(
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "productCategoryId", required = false) Long productCategoryId,
            @RequestParam(value = "productSn", required = false) String productSn,
            @RequestParam(value = "publishStatus", required = false) Integer publishStatus,
            @RequestParam(value = "verifyStatus", required = false) Integer verifyStatus,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {

        PageResult<PmsProduct> pageResult = pmsProductService.list(brandId, keyword, productCategoryId,
                productSn, publishStatus, verifyStatus,
                pageNum, pageSize);
        return Result.success(pageResult);
    }
    @GetMapping("/simpleList")
    @ApiOperation("根据商品名称或货号模糊查询")
    public Result simpleList(@RequestParam(value = "keyword", required = false) String keyword) {
        List<PmsProduct> list = pmsProductService.simpleList(keyword);
        return Result.success(list);
    }

    @PostMapping("/update/deleteStatus")
    @ApiOperation("批量修改删除状态")
    public Result updateDeleteStatus(@RequestParam List<Long> ids, @RequestParam Integer deleteStatus) {
        pmsProductService.updateDeleteStatus(ids, deleteStatus);
        return Result.success();
    }

    @PostMapping("/update/newStatus")
    @ApiOperation("批量设为新品")
    public Result updateNewStatus(@RequestParam List<Long> ids, @RequestParam Integer newStatus) {
        pmsProductService.updateNewStatus(ids, newStatus);
        return Result.success();
    }

    @PostMapping("/update/publishStatus")
    @ApiOperation("批量上下架商品")
    public Result updatePublishStatus(@RequestParam List<Long> ids, @RequestParam Integer publishStatus) {
        pmsProductService.updatePublishStatus(ids, publishStatus);
        return Result.success();
    }

    @PostMapping("/update/recommendStatus")
    @ApiOperation("批量推荐商品")
    public Result updateRecommendStatus(@RequestParam List<Long> ids, @RequestParam Integer recommendStatus) {
        pmsProductService.updateRecommendStatus(ids, recommendStatus);
        return Result.success();
    }

    @PostMapping("/update/verifyStatus")
    @ApiOperation("批量修改审核状态")
    public Result updateVerifyStatus(@RequestParam List<Long> ids, @RequestParam Integer verifyStatus, @RequestParam String detail) {
        pmsProductService.updateVerifyStatus(ids, verifyStatus, detail);
        return Result.success();
    }

    @PostMapping("/update/{id}")
    @ApiOperation("根据ID修改商品信息")
    public Result updateProduct(@PathVariable Long id, @RequestBody PmsProductParam productParam) {
        pmsProductService.updateProduct(id, productParam);
        return Result.success();
    }

    @GetMapping("/updateInfo/{id}")
    @ApiOperation("根据商品id获取商品编辑信息")
    public Result getUpdateInfo(@PathVariable Long id) {
        PmsProductResult result = pmsProductService.getUpdateInfo(id);
        return Result.success(result);
    }
}
