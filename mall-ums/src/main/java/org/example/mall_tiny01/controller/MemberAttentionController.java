package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.dto.MemberBrandAttention;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.mapper.UmsMemberMapper;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.service.MemberAttentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "MemberAttentionController", description = "会员关注管理")
@RestController
@RequestMapping("/member/attention")
public class MemberAttentionController {
    
    @Autowired
    private MemberAttentionService memberAttentionService;
    
    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @PostMapping("/add")
    @ApiOperation("添加品牌关注")
    public Result add(@RequestBody MemberBrandAttention attention) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            memberAttentionService.add(member.getId(), attention);
            return Result.success("关注成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/clear")
    @ApiOperation("清空当前用户品牌关注列表")
    public Result clear() {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            memberAttentionService.clear(member.getId());
            return Result.success("清空成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    @ApiOperation("取消品牌关注")
    public Result delete(@RequestParam(value = "brandId", required = false) Long brandId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            memberAttentionService.delete(member.getId(), brandId);
            return Result.success("取消关注成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail")
    @ApiOperation("根据品牌ID获取品牌关注详情")
    public Result<MemberBrandAttention> detail(@RequestParam Long brandId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            MemberBrandAttention data = memberAttentionService.detail(member.getId(), brandId);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation("分页查询当前用户品牌关注列表")
    public Result<PageResult<MemberBrandAttention>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);
            
            PageResult<MemberBrandAttention> pageResult = memberAttentionService.list(member.getId(), pageNum, pageSize);
            return Result.success(pageResult);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private UmsMember getMemberByUsername(String username) {
        if (username == null) {
            throw new RuntimeException("用户未登录");
        }
        
        UmsMember member = umsMemberMapper.selectByUsername(username);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return member;
    }
}
