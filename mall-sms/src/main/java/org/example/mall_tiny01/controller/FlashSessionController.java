package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.dto.SmsFlashPromotionSessionDetail;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotionSession;
import org.example.mall_tiny01.service.FlashSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flashSession")
@Api(tags = "限时购场次管理")
public class FlashSessionController {
    @Autowired
    private FlashSessionService flashSessionService;
    
    @PostMapping("/create")
    @ApiOperation("添加场次")
    public Result add(@RequestBody SmsFlashPromotionSession session){
        flashSessionService.save(session);
        return Result.success("添加成功");
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除场次")
    public Result delete(@PathVariable Long id){
        flashSessionService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("获取全部场次")
    public Result listAll(){
        List<SmsFlashPromotionSession> result = flashSessionService.listAll();
        return Result.success(result);
    }

    @GetMapping("/selectList")
    @ApiOperation("获取全部可选场次及其数量")
    public Result selectList(@RequestParam(value = "flashPromotionId", required = false) Long flashPromotionId){
        List<SmsFlashPromotionSessionDetail> result = flashSessionService.selectList(flashPromotionId);
        return Result.success(result);
    }

    @PostMapping("/update/status/{id}")
    @ApiOperation("根据ID修改场次启用状态")
    public Result updateStatus(
            @PathVariable Long id,
            @RequestParam(value = "status", required = false) Integer status) {
        
        flashSessionService.updateStatus(id, status);
        return Result.success("修改成功");
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改场次")
    public Result update(
            @PathVariable Long id,
            @RequestBody SmsFlashPromotionSession session) {
        
        flashSessionService.update(id, session);
        return Result.success("修改成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("获取场次详情")
    public Result getById(@PathVariable Long id){
        SmsFlashPromotionSession result = flashSessionService.getById(id);
        return Result.success(result);
    }
}
