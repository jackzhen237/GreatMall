package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.SmsHomeBrand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HomeBrandService {
    int createHomeBrand(List<SmsHomeBrand> homeBrandList);
    
    int deleteHomeBrandBatch(Long[] ids);
    
    PageResult<SmsHomeBrand> list(String brandName, Integer recommendStatus, 
                                   Integer pageNum, Integer pageSize);
    
    int updateRecommendStatus(Long[] ids, Integer recommendStatus);
    
    int updateSort(Long id, Integer sort);
}
