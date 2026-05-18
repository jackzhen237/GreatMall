package org.example.mall_tiny01.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.dto.SkuStockReduceDto;
import org.example.mall_tiny01.mbg.mapper.PmsSkuStockMapper;
import org.example.mall_tiny01.mbg.model.PmsSkuStock;
import org.example.mall_tiny01.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sku")
public class SkuController {
    @Autowired
    private SkuService skuService;

    @Autowired
    private PmsSkuStockMapper skuStockMapper;

    @PostMapping("/update/{pid}")
    @ApiOperation("根据商品ID批量更新sku库存信息")
    public Result updateStock(@PathVariable Long pid, @RequestBody List<PmsSkuStock> skuStockList) {
        skuService.updateStock(pid, skuStockList);
        return Result.success();
    }

    @GetMapping("/{pid}")
    @ApiOperation("根据商品ID及sku编码模糊搜索sku库存")
    public Result<List<PmsSkuStock>> list(
            @PathVariable Long pid,
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<PmsSkuStock> list = skuService.list(pid, keyword);
        return Result.success(list);
    }

    @PostMapping("/stock/deduct")
    @ApiOperation("扣减单个SKU库存（防超卖），供OMS下单时远程调用")
    public Result<Integer> deductStock(@RequestParam Long skuId,
                                       @RequestParam Integer quantity) {
        int count = skuStockMapper.updateStock(skuId, quantity);
        return Result.success(count);
    }

    @PostMapping("/stock/batchReduce")
    @ApiOperation("批量扣减多个SKU库存（CASE WHEN 一条SQL完成，乐观锁防超卖），供OMS下单时远程调用")
    public Result<Integer> batchReduceStock(@RequestBody List<SkuStockReduceDto> reduceList) {
        // 一条 UPDATE 语句批量修改多个 SKU，每个 SKU 带 stock >= quantity 乐观锁
        // 返回实际成功扣减的行数
        int count = skuStockMapper.batchUpdateSkuStock(reduceList);
        return Result.success(count);
    }
}