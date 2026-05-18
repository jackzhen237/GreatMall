package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.alibaba.fastjson.JSON;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.CmsSubjectMapper;
import org.example.mall_tiny01.mbg.model.CmsSubject;
import org.example.mall_tiny01.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectServiceimpl implements SubjectService {
    @Autowired
    private CmsSubjectMapper cmsSubjectMapper;

    @Autowired
    private CacheQueryService cacheService;

    @Override
    public PageResult<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<CmsSubject> list = cmsSubjectMapper.list(keyword, null);

        PageInfo<CmsSubject> pageInfo = new PageInfo<>(list);

        PageResult<CmsSubject> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());

        return result;
    }

    @Override
    public List<CmsSubject> listAll() {
        String json = cacheService.query("subject:all", String.class,
                () -> JSON.toJSONString(cmsSubjectMapper.listAll()));
        if (json == null) return null;
        return JSON.parseArray(json, CmsSubject.class);
    }
}
