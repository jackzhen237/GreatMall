package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.mbg.model.UmsMemberProductCollection;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.mapper.UmsMemberMapper;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.service.MemberCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "MemberCollectionController", description = "会员商品收藏管理")
@RestController
@RequestMapping("/member/productCollection")
public class MemberCollectionController {

    @Autowired
    private MemberCollectionService memberCollectionService;

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @PostMapping("/add")
    @ApiOperation("添加商品收藏")
    public Result add(@RequestBody UmsMemberProductCollection productCollection) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            productCollection.setMemberId(member.getId());
            productCollection.setMemberNickname(member.getNickname());
            productCollection.setMemberIcon(member.getIcon());

            memberCollectionService.add(productCollection);
            return Result.success("收藏成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/clear")
    @ApiOperation("清空当前用户商品收藏列表")
    public Result clear() {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            memberCollectionService.clear(member.getId());
            return Result.success("清空成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    @ApiOperation("删除商品收藏")
    public Result delete(@RequestParam Long productId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            memberCollectionService.delete(member.getId(), productId);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail")
    @ApiOperation("显示商品收藏详情")
    public Result<UmsMemberProductCollection> detail(@RequestParam Long productId) {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            UmsMemberProductCollection detail = memberCollectionService.detail(member.getId(), productId);
            return Result.success(detail);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation("获取会员收藏列表")
    public Result<List<UmsMemberProductCollection>> list() {
        try {
            String username = UserContext.getUsername();
            UmsMember member = getMemberByUsername(username);

            List<UmsMemberProductCollection> list = memberCollectionService.list(member.getId());
            return Result.success(list);
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