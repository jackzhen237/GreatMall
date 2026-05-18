package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.dto.*;
import org.example.mall_tiny01.mbg.model.UmsAdmin;
import org.example.mall_tiny01.service.UmsAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class UmsAdminController {
    @Autowired
    private UmsAdminService umsAdminService;
    @PostMapping("/delete/{id}")
    @ApiOperation("删除指定用户")
    public Result delete(@PathVariable Long id) {
     umsAdminService.deleteByPrimaryKey(id);
     return Result.success();
    }

    @GetMapping("/info")
    @ApiOperation("获取当前登录用户信息")
    public Result info() {
        // 1. 直接从拦截器里获取当前登录的用户名，不需要前端传参
        String username = UserContext.getUsername();
        
        if (username == null) {
            return Result.error("用户未登录");
        }
        
        AdminInfo adminInfo = umsAdminService.getAdminInfo(username);
        return Result.success(adminInfo);
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result register(@RequestBody AdminDTO adminDTO) {
        umsAdminService.register(adminDTO);
        return Result.success();
    }

   @PostMapping("/login")
   @ApiOperation("用户登录")
   public Result login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
       try {
           // 兼容代理获取真实 IP
           String ip = request.getHeader("X-Forwarded-For");
           if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
               ip = request.getHeader("Proxy-Client-IP");
           }
           if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
               ip = request.getHeader("WL-Proxy-Client-IP");
           }
           if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
               ip = request.getRemoteAddr();
           }
           if (ip != null && ip.contains(",")) {
               ip = ip.split(",")[0];
           }

           String userAgent = request.getHeader("User-Agent");
           String token = umsAdminService.login(loginDTO.getUsername(), loginDTO.getPassword(), ip, userAgent);
           
           // 修改这里：把 token 封装到 Map 对象中，让 data 成为 object 类型
           Map<String, String> tokenData = new HashMap<>();
           tokenData.put("token", token);
           tokenData.put("tokenHead", "Bearer ");
           return Result.success(tokenData);
       } catch (RuntimeException e) {
           return Result.error(e.getMessage());
       }
   }

   @PostMapping("/logout")
    @ApiOperation("用户登出")
   public Result logout() {
        return Result.success("登出成功");
   }
   @GetMapping("/refreshToken")
   @ApiOperation("刷新token")
   public Result refreshToken(){
        String token=umsAdminService.refreshToken();
        return Result.success(token);
   }
    @GetMapping("/list")
    @ApiOperation("分页查询")
    public Result list(String keyword, Integer pageNum, Integer pageSize) {
        PageResult<UmsAdmin>result =umsAdminService.list(keyword, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/role/update")
    @ApiOperation("给用户分配角色")
    public Result updateRole(@RequestParam Long adminId, @RequestParam Long roleIds) {
        umsAdminService.updateRole(adminId, roleIds);
        return Result.success();
    }

    @GetMapping("/role/{adminId}")
    @ApiOperation("获取用户角色")
    public Result getRole(@PathVariable Long adminId) {
        GetDataByAdminId getDataByAdminId = umsAdminService.getRole(adminId);
        return Result.success(getDataByAdminId);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("修改指定用户信息")
    public Result update(@PathVariable Long id, @RequestBody UmsAdmin umsAdmin) {
        umsAdminService.update(id, umsAdmin);
        return Result.success();
    }

    @PostMapping("/updatePassword")
    @ApiOperation("修改密码")
    public Result updatePassword(@RequestBody UpdatePasswordDTO passwordDTO) {
        umsAdminService.updatePassword(passwordDTO.getUsername(), passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return Result.success();
    }

    @PostMapping("/updateStatus/{id}")
    @ApiOperation("修改用户状态")
    public Result updateStatus(@PathVariable Long id, Integer status) {
        umsAdminService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/{id:\\d+}") // 修改这里：\\d+ 表示只匹配数字
    @ApiOperation("获取指定用户信息")
    public Result getItem(@PathVariable Long id) {
        UmsAdmin umsAdmin = umsAdminService.selectByPrimaryKey(id);
        return Result.success(umsAdmin);
    }
}
