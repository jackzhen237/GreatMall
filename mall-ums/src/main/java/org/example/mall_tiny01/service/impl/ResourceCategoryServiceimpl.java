package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.service.ResourceCategoryService;
import org.example.mall_tiny01.service.ResourceService;
import org.springframework.stereotype.Service;

import org.example.mall_tiny01.mbg.mapper.UmsResourceCategoryMapper;
import org.example.mall_tiny01.mbg.model.UmsResourceCategory;
import org.example.mall_tiny01.service.ResourceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ResourceCategoryServiceimpl implements ResourceCategoryService {
    @Autowired
    private UmsResourceCategoryMapper umsResourceCategoryMapper;

    @Override
    public void createCategory(UmsResourceCategory category) {
        if (category.getCreateTime() == null) {
            category.setCreateTime(new Date());
        }
        umsResourceCategoryMapper.insertSelective(category);
    }

    @Override
    public void deleteCategory(Long id) {
        umsResourceCategoryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<UmsResourceCategory> listAll() {
        return umsResourceCategoryMapper.listAll();
    }

    @Override
    public void updateCategory(Long id, UmsResourceCategory category) {
        category.setId(id);
        umsResourceCategoryMapper.updateByPrimaryKeySelective(category);
    }
}