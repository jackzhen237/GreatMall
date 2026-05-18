package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.UmsMenuNode;
import org.example.mall_tiny01.mbg.model.UmsMenu;

import java.util.List;

public interface MenuService {
    int createMenu(UmsMenu menu);
    
    int deleteMenu(Long id);
    
    PageResult<UmsMenu> list(Long parentId, Integer pageNum, Integer pageSize);
    
    List<UmsMenuNode> treeList();
    
    int updateMenu(Long id, UmsMenu menu);
    
    int updateHidden(Long id, Integer hidden);
    
    UmsMenu getById(Long id);
}
