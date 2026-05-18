package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.UmsRoleMapper;
import org.example.mall_tiny01.mbg.mapper.UmsRoleMenuRelationMapper;
import org.example.mall_tiny01.mbg.mapper.UmsRoleResourceRelationMapper;
import org.example.mall_tiny01.mbg.model.UmsMenu;
import org.example.mall_tiny01.mbg.model.UmsResource;
import org.example.mall_tiny01.mbg.model.UmsRole;
import org.example.mall_tiny01.mbg.model.UmsRoleMenuRelation;
import org.example.mall_tiny01.mbg.model.UmsRoleResourceRelation;
import org.example.mall_tiny01.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RoleServiceimpl implements RoleService {
    @Autowired
    private UmsRoleMapper umsRoleMapper;
    
    @Autowired
    private UmsRoleMenuRelationMapper umsRoleMenuRelationMapper;
    
    @Autowired
    private UmsRoleResourceRelationMapper umsRoleResourceRelationMapper;

    @Override
    public void createRole(UmsRole role) {
        if (role.getCreateTime() == null) {
            role.setCreateTime(new Date());
        }
        umsRoleMapper.insertSelective(role);
    }

    @Override
    public void deleteRole(List<Long> ids) {
        umsRoleMapper.deleteBatch(ids);
    }

    @Override
    public PageResult<UmsRole> list(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<UmsRole> list = umsRoleMapper.list(keyword);

        PageInfo<UmsRole> pageInfo = new PageInfo<>(list);

        PageResult<UmsRole> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());

        return result;
    }

    @Override
    public List<UmsRole> listAll() {
        return umsRoleMapper.listAll();
    }

    @Override
    public void allocMenu(Long roleId, List<Long> menuIds) {
        umsRoleMenuRelationMapper.deleteByRoleId(roleId);
        
        if (menuIds != null && !menuIds.isEmpty()) {
            List<UmsRoleMenuRelation> relations = new ArrayList<>();
            for (Long menuId : menuIds) {
                UmsRoleMenuRelation relation = new UmsRoleMenuRelation();
                relation.setRoleId(roleId);
                relation.setMenuId(menuId);
                relations.add(relation);
            }
            umsRoleMenuRelationMapper.insertBatch(relations);
        }
    }

    @Override
    public void allocResource(Long roleId, List<Long> resourceIds) {
        umsRoleResourceRelationMapper.deleteByRoleId(roleId);
        
        if (resourceIds != null && !resourceIds.isEmpty()) {
            List<UmsRoleResourceRelation> relations = new ArrayList<>();
            for (Long resourceId : resourceIds) {
                UmsRoleResourceRelation relation = new UmsRoleResourceRelation();
                relation.setRoleId(roleId);
                relation.setResourceId(resourceId);
                relations.add(relation);
            }
            umsRoleResourceRelationMapper.insertBatch(relations);
        }
    }

    @Override
    public List<UmsMenu> listMenu(Long roleId) {
        return umsRoleMapper.listMenu(roleId);
    }

    @Override
    public List<UmsResource> listResource(Long roleId) {
        return umsRoleMapper.listResource(roleId);
    }

    @Override
    public void updateRole(Long id, UmsRole role) {
        role.setId(id);
        umsRoleMapper.updateByPrimaryKeySelective(role);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        UmsRole role = new UmsRole();
        role.setId(id);
        role.setStatus(status);
        umsRoleMapper.updateByPrimaryKeySelective(role);
    }
}