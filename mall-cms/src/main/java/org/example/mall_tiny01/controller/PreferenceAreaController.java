package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.CmsPrefrenceArea;
import org.example.mall_tiny01.service.PreferenceAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prefrenceArea")
public class PreferenceAreaController {
    @Autowired
    private PreferenceAreaService preferenceAreaService;

    @GetMapping("/listAll")
    @ApiOperation("获取所有商品优选")
    public Result listAll() {
        List<CmsPrefrenceArea> list = preferenceAreaService.listAll();
        return Result.success(list);
    }
}
