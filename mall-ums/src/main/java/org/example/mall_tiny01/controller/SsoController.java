package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.dto.LoginDTO;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.mapper.UmsIntegrationConsumeSettingMapper;
import org.example.mall_tiny01.mbg.mapper.UmsMemberMapper;
import org.example.mall_tiny01.mbg.model.UmsIntegrationConsumeSetting;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.service.SsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "SsoController", description = "会员登录注册管理")
@RestController
@RequestMapping("/sso")
public class SsoController {
    @Autowired
    private SsoService ssoService;

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Autowired
    private UmsIntegrationConsumeSettingMapper integrationConsumeSettingMapper;

    @GetMapping("/getAuthCode")
    @ApiOperation("获取验证码")
    public Result getAuthCode(@RequestParam String telephone) {
        String authCode = ssoService.getAuthCode(telephone);
        return Result.success(authCode);
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result login(@RequestParam String username, @RequestParam String password) {
        try {
            String token = ssoService.login(username, password);
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("token", token);
            tokenData.put("tokenHead", "Bearer ");
            return Result.success(tokenData);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    @ApiOperation("获取会员信息（Feign调用时可传username）")
    public Result<UmsMember> info(@RequestParam(value = "username", required = false) String username) {
        if (username == null || username.isEmpty()) {
            username = UserContext.getUsername();
        }
        if (username == null) {
            return Result.error("用户未登录");
        }
        UmsMember member = ssoService.getMemberInfo(username);
        return Result.success(member);
    }

    @GetMapping("/member/{id}")
    @ApiOperation("根据ID获取会员信息")
    public Result<UmsMember> getMemberById(@PathVariable Long id) {
        UmsMember member = umsMemberMapper.selectByPrimaryKey(id);
        if (member == null) {
            return Result.error("会员不存在");
        }
        return Result.success(member);
    }

    @GetMapping("/integrationConsumeSetting/{id}")
    @ApiOperation("获取积分消费设置")
    public Result<UmsIntegrationConsumeSetting> getIntegrationConsumeSetting(@PathVariable Long id) {
        UmsIntegrationConsumeSetting setting = integrationConsumeSettingMapper.selectByPrimaryKey(id);
        return Result.success(setting);
    }

    @GetMapping("/refreshToken")
    @ApiOperation("刷新 token")
    public Result refreshToken() {
        String username = UserContext.getUsername();
        
        if (username == null) {
            return Result.error("用户未登录");
        }
        
        String newToken = ssoService.refreshToken(username);
        return Result.success(newToken);
    }

    @PostMapping("/register")
    @ApiOperation("会员注册")
    public Result register(@RequestParam String username, 
                          @RequestParam String password, 
                          @RequestParam String telephone, 
                          @RequestParam String authCode) {
        try {
            ssoService.register(username, password, telephone, authCode);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updatePassword")
    @ApiOperation("会员修改密码")
    public Result updatePassword(@RequestParam String telephone, 
                                @RequestParam String password, 
                                @RequestParam String authCode) {
        try {
            ssoService.updatePassword(telephone, password, authCode);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
