package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.CmsSubject;
import org.example.mall_tiny01.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject")
public class SubjectController {
    @Autowired
    private SubjectService subjectService;

    @GetMapping("/list")
    @ApiOperation("根据专题名称分页获取商品专题")
    public Result<PageResult<CmsSubject>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        PageResult<CmsSubject> pageResult = subjectService.list(keyword, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/listAll")
    @ApiOperation("获取全部商品专题")
    public Result<List<CmsSubject>> listAll() {
        List<CmsSubject> list = subjectService.listAll();
        return Result.success(list);
    }
}