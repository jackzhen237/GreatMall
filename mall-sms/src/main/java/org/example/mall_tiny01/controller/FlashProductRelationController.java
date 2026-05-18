package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotionProductRelation;
import org.example.mall_tiny01.service.FlashProductRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flashProductRelation")
@Api(tags = "限时购和商品关系管理")
public class FlashProductRelationController {
    @Autowired
    private FlashProductRelationService flashProductRelationService;

    @PostMapping("/create")
    @ApiOperation("批量选择商品添加关联")
    public Result add(@RequestBody List<SmsFlashPromotionProductRelation> relationList){
        flashProductRelationService.save(relationList);
        return Result.success("添加成功");
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("根据ID删除关联信息")
    public Result delete(@PathVariable Long id){
        flashProductRelationService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("分页查询不同场次关联及商品信息")
    public Result list(
            @RequestParam(value = "flashPromotionId") Long flashPromotionId,
            @RequestParam(value = "flashPromotionSessionId") Long flashPromotionSessionId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        
        PageResult<SmsFlashPromotionProductRelation> result = flashProductRelationService.list(flashPromotionId, flashPromotionSessionId, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("根据ID修改关联信息")
    public Result update(
            @PathVariable Long id,
            @RequestBody SmsFlashPromotionProductRelation relation) {
        
        flashProductRelationService.update(id, relation);
        return Result.success("修改成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("获取关联商品促销信息")
    public Result getById(@PathVariable Long id){
        SmsFlashPromotionProductRelation result = flashProductRelationService.getById(id);
        return Result.success(result);
    }
}
