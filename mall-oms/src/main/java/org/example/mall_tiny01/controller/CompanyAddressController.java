package org.example.mall_tiny01.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.OmsCompanyAddress;
import org.example.mall_tiny01.service.CompanyAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("/companyAddress")
public class CompanyAddressController {
    @Autowired
    private CompanyAddressService companyAddressService;

    @GetMapping("/list")
    @ApiOperation("获取所有收货地址")
    public Result<List<OmsCompanyAddress>> list() {
        List<OmsCompanyAddress> list = companyAddressService.list();
        return Result.success(list);
    }
}