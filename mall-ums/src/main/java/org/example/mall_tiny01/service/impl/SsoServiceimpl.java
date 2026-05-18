package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.config.JwtConfig;
import org.example.mall_tiny01.config.RedisConfig;
import org.example.mall_tiny01.mbg.mapper.UmsMemberMapper;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.service.SsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SsoServiceimpl implements SsoService {
    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private RedisConfig redisConfig;

    @Autowired
    private CacheQueryService cacheService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public String login(String username, String password) {
        UmsMember member = umsMemberMapper.selectByUsername(username);
        
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        String token = jwtConfig.getToken(username);
        
        UserContext.setUsername(username);
        
        return token;
    }
    
    @Override
    public String getAuthCode(String telephone) {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        String authCode = String.valueOf(code);
        
        redisConfig.set(telephone, authCode, 5, TimeUnit.MINUTES);
        
        return authCode;
    }

    @Override
    public UmsMember getMemberInfo(String username) {
        return cacheService.query("member:username:" + username, UmsMember.class,
                () -> umsMemberMapper.selectByUsername(username));
    }

    @Override
    public String refreshToken(String username) {
        return jwtConfig.getToken(username);
    }

    @Override
    public void register(String username, String password, String telephone, String authCode) {
        UmsMember existMember = umsMemberMapper.selectByUsername(username);
        if (existMember != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        String storedCode = redisConfig.get(telephone);
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        
        if (!authCode.equals(storedCode)) {
            throw new RuntimeException("验证码验证失败");
        }
        
        UmsMember member = new UmsMember();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password));
        member.setPhone(telephone);
        
        umsMemberMapper.insertSelective(member);
        
        redisConfig.delete(telephone);
    }

    @Override
    public void updatePassword(String telephone, String password, String authCode) {
        String storedCode = redisConfig.get(telephone);
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        
        if (!authCode.equals(storedCode)) {
            throw new RuntimeException("验证码验证失败");
        }
        
        UmsMember member = umsMemberMapper.selectByPhone(telephone);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }

        member.setPassword(passwordEncoder.encode(password));
        umsMemberMapper.updateByPrimaryKeySelective(member);
        
        redisConfig.delete(telephone);
    }
}
