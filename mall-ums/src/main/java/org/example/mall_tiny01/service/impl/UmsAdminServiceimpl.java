package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Jwts;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.config.JwtConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.example.mall_tiny01.dto.AdminDTO;
import org.example.mall_tiny01.dto.AdminInfo;
import org.example.mall_tiny01.dto.GetDataByAdminId;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.UmsAdminMapper;
import org.example.mall_tiny01.mbg.model.UmsAdmin;
import org.example.mall_tiny01.mbg.model.UmsAdminLoginLog;
import org.example.mall_tiny01.service.UmsAdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UmsAdminServiceimpl implements UmsAdminService {
    @Override
    public String refreshToken() {
        // 1. 从 UserContext 中获取当前登录用户的用户名
        String username = UserContext.getUsername();
        
        // 2. 根据用户名重新生成新 Token
        return jwtConfig.getToken(username);
    }

    @Override
    public AdminInfo getAdminInfo(String username) {
        AdminInfo adminInfo = new AdminInfo();
        adminInfo.setName(username); // 赋值给 name
        
        Long adminId = umsAdminMapper.getAdminId(username);
        
        if (adminId != null) {
            String role = umsAdminMapper.getRole(adminId);
            if (role != null && !role.trim().isEmpty()) {
                adminInfo.getRoles().add(role.trim());
                adminInfo.setMenus(umsAdminMapper.getMenus(role.trim()));
            }
            adminInfo.setAvatar(umsAdminMapper.getIcon(adminId)); // 赋值给 avatar
        }
        
        return adminInfo;
    }

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public String login(String username, String password,String ip,String useragent) {
        String dbPassword = umsAdminMapper.getPassword(username);
        if (dbPassword == null) {
            throw new RuntimeException("用户不存在");
        }
        // BCrypt 匹配：数据库存的是 $2a$10$... 哈希值，用 matches() 比对用户输入的明文
        if (!passwordEncoder.matches(password, dbPassword)) {
            throw new RuntimeException("密码错误");
        }
        String token = jwtConfig.getToken(username);
        //向ums_admin_login_log表插入数据
        UmsAdminLoginLog  umsAdminLoginLog = new UmsAdminLoginLog();
        //根据username查找对应的adminId，并将其传入道umsAdminLoginLog对象中
        umsAdminLoginLog.setAdminId(umsAdminMapper.getAdminId(username));
        //根据username查找对应的createtime，并将其传入道umsAdminLoginLog对象中
        umsAdminLoginLog.setCreateTime(umsAdminMapper.getCreateTime(umsAdminLoginLog.getAdminId()));
        //获取当前ip传入到umsAdminLoginLog对象中
        umsAdminLoginLog.setIp(ip);
        //调用该类下面的getAddressById方法获取adress
        umsAdminLoginLog.setAddress(getAddressByIp(ip));
        //将useragent传入umsAdminLoginLog对象中
        umsAdminLoginLog.setUserAgent(useragent);
        umsAdminMapper.insertLoginLog(umsAdminLoginLog);

        //更新admin表中的login_time
        umsAdminMapper.updateLoginTime(umsAdminLoginLog.getAdminId(), new Date());
        return token;
    }
        /**
     * 根据 IP 获取归属地（使用免费的太平洋 IP 库）
     */
    private String getAddressByIp(String ip) {
        // 1. 如果是本地 IP，直接返回
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "内网 IP";
        }
        try {
            // 2. 请求接口
            String urlStr = "https://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true";
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("accept", "*/*");

            // 注意：这个接口返回的是 GBK 编码，必须指定编码否则乱码
            java.io.BufferedReader in = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), "GBK"));

            String line;
            StringBuilder result = new StringBuilder();
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

            // 3. 简单解析 JSON，提取 addr 字段
            String json = result.toString();
            int start = json.indexOf("\"addr\":\"");
            if (start != -1) {
                start += 8;
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
            }
        } catch (Exception e) {
            // 如果接口挂了，不要影响登录，返回“未知”即可
            return "未知";
        }
        return "未知";
    }


    @Override
    public UmsAdmin selectByPrimaryKey(Long id) {
        UmsAdmin umsAdmin = umsAdminMapper.selectByPrimaryKey(id);
        return umsAdmin;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        //调用umsAdminMapper的updateStatus方法
        umsAdminMapper.updateStatus(id, status);
    }

    @Override
    public void updatePassword(String username, String oldPassword, String newPassword) {
        //先验证oldPassword对不对
        String dbPassword = umsAdminMapper.getPassword(username);
        if (dbPassword == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!dbPassword.equals(oldPassword)) {
            throw new RuntimeException("旧密码错误");
        }
        //将username的用户的密码改为newPassword
        umsAdminMapper.updatePassword(username, oldPassword, newPassword);
    }

    @Override
    public void update(Long id, UmsAdmin umsAdmin) {
        //根据修改id为#{id}的admin的信息，新信息为umsAdmin
        umsAdminMapper.updateByPrimaryKeySelective(umsAdmin);
    }

    @Override
    public GetDataByAdminId getRole(Long adminId) {
        //根据adminId查询role Id，然后再根据roleId查询roleName
        GetDataByAdminId getDataByAdminId = new GetDataByAdminId();
        getDataByAdminId.setName(umsAdminMapper.getRole(adminId));
        //根据admin查询roleId
        getDataByAdminId.setId(umsAdminMapper.getRoleId(adminId));
        //根据roleid查询sort
        getDataByAdminId.setSort(umsAdminMapper.getSort(getDataByAdminId.getId()));
        //根据roleid查询status
        getDataByAdminId.setStatus(umsAdminMapper.getStatus(getDataByAdminId.getId()));
        //根据roleid查询description
        getDataByAdminId.setDescription(umsAdminMapper.getDescription(getDataByAdminId.getId()));
        //根据adminId查询createtime
        getDataByAdminId.setCreateTime(umsAdminMapper.getCreateTime(adminId));
        //根据roleId查找该用户所属角色的数量
        getDataByAdminId.setAdminCount(umsAdminMapper.getAdminCount(getDataByAdminId.getId()));
        return getDataByAdminId;
    }

    @Override
    public void updateRole(Long adminId, Long roleIds) {
        umsAdminMapper.updateRole(adminId,roleIds);
    }

    @Override
    public PageResult<UmsAdmin> list(String keyword, Integer pageNum, Integer pageSize) {
        // 1. 开启分页（PageHelper 会自动拦截下面的 SQL 并加上 limit）
        PageHelper.startPage(pageNum, pageSize);

        // 2. 执行查询（注意：Mapper 接口中对应的方法只需接收 keyword 即可）
        List<UmsAdmin> list = umsAdminMapper.list(keyword);

        // 3. 使用 PageInfo 包装查询结果，获取分页信息
        PageInfo<UmsAdmin> pageInfo = new PageInfo<>(list);

        // 4. 封装成 PageResult 返回
        PageResult<UmsAdmin> pageResult = new PageResult<>();
        pageResult.setList(pageInfo.getList());
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setTotalPage(pageInfo.getPages());

        return pageResult;
    }

    @Autowired
    private UmsAdminMapper umsAdminMapper;

    @Override
    public void register(AdminDTO adminDTO) {
        UmsAdmin umsAdmin = new UmsAdmin();
        //将adminDTO中的属性赋值到UmsAdmin对象中
        BeanUtils.copyProperties(adminDTO, umsAdmin);
        //将createtime设置为当前时间，status为1，loginTime设置为null
        umsAdmin.setCreateTime(new Date());
        umsAdmin.setStatus(1);
        umsAdmin.setLoginTime(null);
        umsAdminMapper.insert(umsAdmin);
    }

    @Override
    public void deleteByPrimaryKey(Long id) {
        umsAdminMapper.deleteByPrimaryKey(id);
    }
}
