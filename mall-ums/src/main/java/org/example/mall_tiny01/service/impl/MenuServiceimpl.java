package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.UmsMenuNode;
import org.example.mall_tiny01.mbg.mapper.UmsMenuMapper;
import org.example.mall_tiny01.mbg.model.UmsMenu;
import org.example.mall_tiny01.service.MenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceimpl implements MenuService {
    @Autowired
    private UmsMenuMapper umsMenuMapper;

    @Override
    public int createMenu(UmsMenu menu) {
        return umsMenuMapper.insertSelective(menu);
    }

    @Override
    public int deleteMenu(Long id) {
        return umsMenuMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageResult<UmsMenu> list(Long parentId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<UmsMenu> list = umsMenuMapper.list(parentId);
        
        PageInfo<UmsMenu> pageInfo = new PageInfo<>(list);
        
        PageResult<UmsMenu> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public List<UmsMenuNode> treeList() {
        List<UmsMenu> allMenus = umsMenuMapper.getAllMenus();
        
        List<UmsMenuNode> menuNodes = new ArrayList<>();
        for (UmsMenu menu : allMenus) {
            if (menu.getParentId() == 0) {
                UmsMenuNode node = new UmsMenuNode();
                BeanUtils.copyProperties(menu, node);
                node.setChildren(getChildren(menu, allMenus));
                menuNodes.add(node);
            }
        }
        
        return menuNodes;
    }

    private List<UmsMenuNode> getChildren(UmsMenu parent, List<UmsMenu> allMenus) {
        return allMenus.stream()
            .filter(menu -> menu.getParentId().equals(parent.getId()))
            .map(menu -> {
                UmsMenuNode node = new UmsMenuNode();
                BeanUtils.copyProperties(menu, node);
                node.setChildren(getChildren(menu, allMenus));
                return node;
            })
            .collect(Collectors.toList());
    }

    @Override
    public int updateMenu(Long id, UmsMenu menu) {
        menu.setId(id);
        return umsMenuMapper.updateByPrimaryKeySelective(menu);
    }

    @Override
    public int updateHidden(Long id, Integer hidden) {
        UmsMenu menu = new UmsMenu();
        menu.setId(id);
        menu.setHidden(hidden);
        return umsMenuMapper.updateByPrimaryKeySelective(menu);
    }

    @Override
    public UmsMenu getById(Long id) {
        return umsMenuMapper.selectByPrimaryKey(id);
    }
}
