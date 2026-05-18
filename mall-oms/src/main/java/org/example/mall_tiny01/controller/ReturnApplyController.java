package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.OmsOrderReturnApply;
import org.example.mall_tiny01.service.ReturnApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/returnApply")
public class ReturnApplyController {
    @Autowired
    private ReturnApplyService returnApplyService;

    @PostMapping("/create")
    @ApiOperation("申请退货")
    public Result create(@RequestBody OmsOrderReturnApply returnApply) {
        try {
            String username = UserContext.getUsername();
            if (username == null) {
                return Result.error("用户未登录");
            }
            
            returnApply.setMemberUsername(username);
            returnApply.setCreateTime(new Date());
            returnApply.setStatus(0);
            
            returnApplyService.create(returnApply);
            return Result.success("申请成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除退货申请")
    public Result deleteApply(@RequestParam List<Long> ids) {
        returnApplyService.deleteApply(ids);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("分页查询退货申请")
    public Result<PageResult<OmsOrderReturnApply>> list(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "receiverKeyword", required = false) String receiverKeyword,
            @RequestParam(value = "handleMan", required = false) String handleMan,
            @RequestParam(value = "handleTime", required = false) String handleTime,
            @RequestParam(value = "createTime", required = false) String createTime,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        PageResult<OmsOrderReturnApply> pageResult = returnApplyService.list(id, status, receiverKeyword,
                handleMan, handleTime, createTime,
                pageNum, pageSize);
        return Result.success(pageResult);
    }

    @PostMapping("/update/status/{id}")
    @ApiOperation("修改退货申请状态")
    public Result updateStatus(@PathVariable Long id, @RequestBody OmsOrderReturnApply returnApply) {
        returnApplyService.updateStatus(id, returnApply);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("获取退货申请详情")
    public Result<OmsOrderReturnApply> getApply(@PathVariable Long id) {
        OmsOrderReturnApply apply = returnApplyService.getApply(id);
        return Result.success(apply);
    }
}