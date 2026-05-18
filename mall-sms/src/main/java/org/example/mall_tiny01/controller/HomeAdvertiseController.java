package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.SmsHomeAdvertise;
import org.example.mall_tiny01.service.HomeAdvertiseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home/advertise")
@ApiOperation("首页广告管理")
public class HomeAdvertiseController {
    @Autowired
    private HomeAdvertiseService homeAdvertiseService;

    @PostMapping("/create")
    @ApiOperation("添加广告")
    public Result createAdvertise(@RequestBody SmsHomeAdvertise advertise) {
        homeAdvertiseService.createAdvertise(advertise);
        return Result.success();
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除广告")
    public Result deleteAdvertise(@RequestParam("ids") Long[] ids) {
        homeAdvertiseService.deleteAdvertiseBatch(ids);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("分页查询首页广告")
    public Result list(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        
        PageResult<SmsHomeAdvertise> list = homeAdvertiseService.list(name, type, endTime, pageNum, pageSize);
        return Result.success(list);
    }

    @PostMapping("/update/status/{id}")
    @ApiOperation("修改上下线状态")
    public Result updateStatus(@PathVariable Long id, @RequestParam("status") Integer status) {
        homeAdvertiseService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/update/{id}")
    @ApiOperation("根据ID修改广告")
    public Result updateAdvertise(@PathVariable Long id, @RequestBody SmsHomeAdvertise advertise) {
        homeAdvertiseService.updateAdvertise(id, advertise);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取广告详情")
    public Result getById(@PathVariable Long id) {
        SmsHomeAdvertise advertise = homeAdvertiseService.getById(id);
        return Result.success(advertise);
    }
}
