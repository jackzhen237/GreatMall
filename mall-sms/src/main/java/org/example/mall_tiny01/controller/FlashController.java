package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.FlashPromotionDTO;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotion;
import org.example.mall_tiny01.service.FlashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flash")
@Api(tags = "限时购活动管理")
public class FlashController {
    @Autowired
    private FlashService flashService;
    
    @PostMapping("/create")
    @ApiOperation("添加活动")
    public Result add(@RequestBody FlashPromotionDTO flashPromotionDTO){
        flashService.save(flashPromotionDTO);
        return Result.success("添加成功");
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除活动")
    public Result delete(@PathVariable Long id){
        flashService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("根据活动名称分页查询活动")
    public Result list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        
        PageResult<SmsFlashPromotion> result = flashService.list(keyword, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/update/status/{id}")
    @ApiOperation("修改活动上下线状态")
    public Result updateStatus(
            @PathVariable Long id,
            @RequestParam(value = "status", required = false) Integer status) {
        
        flashService.updateStatus(id, status);
        return Result.success("修改成功");
    }

    @PostMapping("/update/{id}")
    @ApiOperation("编辑活动")
    public Result update(
            @PathVariable Long id,
            @RequestBody FlashPromotionDTO flashPromotionDTO) {
        
        flashService.update(id, flashPromotionDTO);
        return Result.success("修改成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("获取活动详情")
    public Result getById(@PathVariable Long id){
        SmsFlashPromotion result = flashService.getById(id);
        return Result.success(result);
    }
}
