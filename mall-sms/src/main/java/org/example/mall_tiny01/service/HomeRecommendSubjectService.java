package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.SmsHomeRecommendSubject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HomeRecommendSubjectService {
    int createHomeRecommendSubject(List<SmsHomeRecommendSubject> recommendSubjectList);
    
    int deleteHomeRecommendSubjectBatch(Long[] ids);
    
    PageResult<SmsHomeRecommendSubject> list(String subjectName, Integer recommendStatus,
                                              Integer pageNum, Integer pageSize);
    
    int updateRecommendStatus(Long[] ids, Integer recommendStatus);
    
    int updateSort(Long id, Integer sort);
}
