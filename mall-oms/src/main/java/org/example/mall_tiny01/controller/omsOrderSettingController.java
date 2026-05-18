package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.OmsOrderSetting;
import org.example.mall_tiny01.service.OmsOrderSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orderSetting")
@ApiOperation("订单设置管理")
public class omsOrderSettingController {

    @Autowired
    private OmsOrderSettingService orderSettingService;

    @GetMapping("/{id}")
    @ApiOperation("获取指定订单设置")
    public Result getItem(@PathVariable Long id) {
        OmsOrderSetting setting = orderSettingService.getItem(id);
        return Result.success(setting);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改指定订单设置")
    public Result update(@PathVariable Long id, @RequestBody OmsOrderSetting orderSetting) {
        orderSettingService.update(id, orderSetting);
        return Result.success("操作成功");
    }
}
