package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.SmsHomeRecommendSubjectMapper;
import org.example.mall_tiny01.mbg.model.SmsHomeRecommendSubject;
import org.example.mall_tiny01.service.HomeRecommendSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeRecommendSubjectServiceimpl implements HomeRecommendSubjectService {
    @Autowired
    private SmsHomeRecommendSubjectMapper smsHomeRecommendSubjectMapper;

    @Override
    public int createHomeRecommendSubject(List<SmsHomeRecommendSubject> recommendSubjectList) {
        int count = 0;
        for (SmsHomeRecommendSubject recommendSubject : recommendSubjectList) {
            count += smsHomeRecommendSubjectMapper.insertSelective(recommendSubject);
        }
        return count;
    }

    @Override
    public int deleteHomeRecommendSubjectBatch(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += smsHomeRecommendSubjectMapper.deleteByPrimaryKey(id);
        }
        return count;
    }

    @Override
    public PageResult<SmsHomeRecommendSubject> list(String subjectName, Integer recommendStatus,
                                                     Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<SmsHomeRecommendSubject> list = smsHomeRecommendSubjectMapper.list(subjectName, recommendStatus);
        
        PageInfo<SmsHomeRecommendSubject> pageInfo = new PageInfo<>(list);
        
        PageResult<SmsHomeRecommendSubject> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public int updateRecommendStatus(Long[] ids, Integer recommendStatus) {
        return smsHomeRecommendSubjectMapper.updateRecommendStatus(ids, recommendStatus);
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeRecommendSubject recommendSubject = new SmsHomeRecommendSubject();
        recommendSubject.setId(id);
        recommendSubject.setSort(sort);
        return smsHomeRecommendSubjectMapper.updateByPrimaryKeySelective(recommendSubject);
    }
}
