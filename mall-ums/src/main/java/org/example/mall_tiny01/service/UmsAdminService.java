package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.AdminDTO;
import org.example.mall_tiny01.dto.AdminInfo;
import org.example.mall_tiny01.dto.GetDataByAdminId;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.UmsAdmin;
import org.springframework.stereotype.Service;

@Service
public interface UmsAdminService {
   public void deleteByPrimaryKey(Long id);

   void register(AdminDTO adminDTO);

    PageResult<UmsAdmin> list(String keyword, Integer pageNum, Integer pageSize);

    void updateRole(Long adminId, Long roleIds);

    GetDataByAdminId getRole(Long adminId);

    void update(Long id, UmsAdmin umsAdmin);

    void updatePassword(String username, String oldPassword, String newPassword);

    void updateStatus(Long id, Integer status);

    UmsAdmin selectByPrimaryKey(Long id);

    String login(String username, String password,String ip,String userAgent);

    AdminInfo getAdminInfo(String  username);

 String refreshToken();

}
