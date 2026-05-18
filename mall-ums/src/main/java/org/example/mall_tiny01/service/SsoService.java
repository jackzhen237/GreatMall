package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.UmsMember;

public interface SsoService {
     String login(String username, String password) ;

    /**
     * 获取验证码
     * @param telephone 手机号
     * @return 验证码
     */
    String getAuthCode(String telephone);

    /**
     * 获取会员信息
     * @param username 用户名
     * @return 会员信息
     */
    UmsMember getMemberInfo(String username);

    /**
     * 刷新 token
     * @param username 用户名
     * @return 新的 token
     */
    String refreshToken(String username);

    /**
     * 会员注册
     * @param username 用户名
     * @param password 密码
     * @param telephone 手机号
     * @param authCode 验证码
     */
    void register(String username, String password, String telephone, String authCode);

    /**
     * 会员修改密码
     * @param telephone 手机号
     * @param password 新密码
     * @param authCode 验证码
     */
    void updatePassword(String telephone, String password, String authCode);

}
