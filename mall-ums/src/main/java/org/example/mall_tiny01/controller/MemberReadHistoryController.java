package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.mbg.model.UmsMemberReadHistory;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.mapper.UmsMemberMapper;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.service.MemberReadHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "MemberReadHistoryController", description = "会员浏览记录管理")
@RestController
@RequestMapping("/member/readHistory")
public class MemberReadHistoryController {

    @Autowired
    private MemberReadHistoryService memberReadHistoryService;

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @PostMapping("/create")
    @ApiOperation("创建浏览记录")
    public Result create(@RequestBody UmsMemberReadHistory readHistory) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            readHistory.setMemberId(member.getId());
            readHistory.setMemberNickname(member.getNickname());
            readHistory.setMemberIcon(member.getIcon());

            memberReadHistoryService.create(readHistory);
            return Result.success("操作成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/clear")
    @ApiOperation("清空浏览记录")
    public Result clear() {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            memberReadHistoryService.clear(member.getId());
            return Result.success("操作成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    @ApiOperation("删除浏览记录")
    public Result delete(@RequestParam List<String> ids) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            memberReadHistoryService.delete(member.getId(), ids);
            return Result.success("操作成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation("分页获取浏览记录")
    public Result<PageResult<UmsMemberReadHistory>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            PageResult<UmsMemberReadHistory> pageResult = memberReadHistoryService.list(member.getId(), pageNum, pageSize);
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