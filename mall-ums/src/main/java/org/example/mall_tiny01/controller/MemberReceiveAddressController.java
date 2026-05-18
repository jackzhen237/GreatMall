package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.mapper.UmsMemberMapper;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.mbg.model.UmsMemberReceiveAddress;
import org.example.mall_tiny01.service.MemberReceiveAddressService;
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
@RequestMapping("/member/address")
public class MemberReceiveAddressController {
    @Autowired
    private MemberReceiveAddressService addressService;

    @Autowired
    private UmsMemberMapper memberMapper;

    @PostMapping("/add")
    @ApiOperation("添加收货地址")
    public Result add(@RequestBody UmsMemberReceiveAddress address) {
        String username = UserContext.getUsername();
        UmsMember member = memberMapper.selectByUsername(username);
        if (member != null) {
            address.setMemberId(member.getId());
        }
        int count = addressService.add(address);
        if (count > 0) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除收货地址")
    public Result delete(@PathVariable Long id) {
        int count = addressService.delete(id);
        if (count > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @GetMapping("/list")
    @ApiOperation("获取所有收货地址")
    public Result<List<UmsMemberReceiveAddress>> list(@RequestParam(required = false) Long memberId) {
        if (memberId == null) {
            String username = UserContext.getUsername();
            UmsMember member = memberMapper.selectByUsername(username);
            if (member != null) {
                memberId = member.getId();
            }
        }
        List<UmsMemberReceiveAddress> list = null;
        if (memberId != null) {
            list = addressService.list(memberId);
        }
        return Result.success(list);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改收货地址")
    public Result update(@PathVariable Long id, @RequestBody UmsMemberReceiveAddress address) {
        int count = addressService.update(id, address);
        if (count > 0) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @GetMapping("/{id}")
    @ApiOperation("获取收货地址详情")
    public Result<UmsMemberReceiveAddress> getDetail(@PathVariable Long id) {
        UmsMemberReceiveAddress address = addressService.getDetail(id);
        return Result.success(address);
    }
}