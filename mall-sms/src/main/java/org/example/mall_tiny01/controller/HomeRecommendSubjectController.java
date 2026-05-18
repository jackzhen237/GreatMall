package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.SmsHomeRecommendSubject;
import org.example.mall_tiny01.service.HomeRecommendSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home/recommendSubject")
@ApiOperation("首页专题推荐管理")
public class HomeRecommendSubjectController {
    @Autowired
    private HomeRecommendSubjectService homeRecommendSubjectService;

    @PostMapping("/create")
    @ApiOperation("批量添加首页专题推荐")
    public Result createHomeRecommendSubject(@RequestBody List<SmsHomeRecommendSubject> recommendSubjectList) {
        homeRecommendSubjectService.createHomeRecommendSubject(recommendSubjectList);
        return Result.success("操作成功");
    }

    @PostMapping("/delete")
    @ApiOperation("批量删除专题推荐")
    public Result deleteHomeRecommendSubject(@RequestParam("ids") Long[] ids) {
        homeRecommendSubjectService.deleteHomeRecommendSubjectBatch(ids);
        return Result.success("操作成功");
    }

    @GetMapping("/list")
    @ApiOperation("分页查询专题推荐")
    public Result list(
            @RequestParam(value = "subjectName", required = false) String subjectName,
            @RequestParam(value = "recommendStatus", required = false) Integer recommendStatus,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 5;
        }
        
        PageResult<SmsHomeRecommendSubject> list = homeRecommendSubjectService.list(subjectName, recommendStatus, pageNum, pageSize);
        return Result.success(list);
    }

    @PostMapping("/update/recommendStatus")
    @ApiOperation("批量修改专题推荐状态")
    public Result updateRecommendStatus(
            @RequestParam("ids") Long[] ids,
            @RequestParam("recommendStatus") Integer recommendStatus) {
        homeRecommendSubjectService.updateRecommendStatus(ids, recommendStatus);
        return Result.success("操作成功");
    }

    @PostMapping("/update/sort/{id}")
    @ApiOperation("修改专题推荐排序")
    public Result updateSort(@PathVariable Long id, @RequestParam(value = "sort", required = false) Integer sort) {
        homeRecommendSubjectService.updateSort(id, sort);
        return Result.success("操作成功");
    }
}
