package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.UmsMenu;
import org.example.mall_tiny01.mbg.model.UmsRole;
import org.example.mall_tiny01.mbg.model.UmsResource;

import java.util.List;

public interface RoleService {
    void createRole(UmsRole role);

    void deleteRole(List<Long> ids);

    PageResult<UmsRole> list(String keyword, Integer pageNum, Integer pageSize);

    List<UmsRole> listAll();

    void allocMenu(Long roleId, List<Long> menuIds);

    void allocResource(Long roleId, List<Long> resourceIds);

    List<UmsMenu> listMenu(Long roleId);

    List<UmsResource> listResource(Long roleId);

    void updateRole(Long id, UmsRole role);

    void updateStatus(Long id, Integer status);
}
