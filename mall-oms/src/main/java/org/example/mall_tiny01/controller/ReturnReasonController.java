package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.OmsOrderReturnReason;
import org.example.mall_tiny01.service.ReturnReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/returnReason")
public class ReturnReasonController {
    @Autowired
    private ReturnReasonService returnReasonService;

    @PostMapping("/create")
    @ApiOperation("添加退货原因")
    public Result createReason(@RequestBody OmsOrderReturnReason reason) {
        returnReasonService.createReason(reason);
        return Result.success();
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除退货原因")
    public Result deleteReason(@RequestParam List<Long> ids) {
        returnReasonService.deleteReason(ids);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("分页查询退货原因")
    public Result<PageResult<OmsOrderReturnReason>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        PageResult<OmsOrderReturnReason> pageResult = returnReasonService.list(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @PostMapping("/update/status")
    @ApiOperation("修改退货原因启用状态")
    public Result updateStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        returnReasonService.updateStatus(ids, status);
        return Result.success();
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改退货原因")
    public Result updateReason(@PathVariable Long id, @RequestBody OmsOrderReturnReason reason) {
        returnReasonService.updateReason(id, reason);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("获取单个退货原因详情信息")
    public Result<OmsOrderReturnReason> getReason(@PathVariable Long id) {
        OmsOrderReturnReason reason = returnReasonService.getReason(id);
        return Result.success(reason);
    }
}
