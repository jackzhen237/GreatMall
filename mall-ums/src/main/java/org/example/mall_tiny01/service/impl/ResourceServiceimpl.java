package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.UmsResourceMapper;
import org.example.mall_tiny01.mbg.model.UmsResource;
import org.example.mall_tiny01.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ResourceServiceimpl implements ResourceService {
    @Autowired
    private UmsResourceMapper umsResourceMapper;

    @Override
    public void createResource(UmsResource resource) {
        if (resource.getCreateTime() == null) {
            resource.setCreateTime(new Date());
        }
        umsResourceMapper.insertSelective(resource);
    }

    @Override
    public void deleteResource(Long id) {
        umsResourceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageResult<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<UmsResource> list = umsResourceMapper.list(categoryId, nameKeyword, urlKeyword);

        PageInfo<UmsResource> pageInfo = new PageInfo<>(list);

        PageResult<UmsResource> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());

        return result;
    }

    @Override
    public List<UmsResource> listAll() {
        return umsResourceMapper.listAll();
    }

    @Override
    public void updateResource(Long id, UmsResource resource) {
        resource.setId(id);
        umsResourceMapper.updateByPrimaryKeySelective(resource);
    }

    @Override
    public UmsResource getResource(Long id) {
        return umsResourceMapper.selectByPrimaryKey(id);
    }
}
