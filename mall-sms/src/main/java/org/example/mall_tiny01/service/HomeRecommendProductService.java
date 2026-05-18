package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.SmsHomeRecommendProduct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HomeRecommendProductService {
    int createHomeRecommendProduct(List<SmsHomeRecommendProduct> recommendProductList);
    
    int deleteHomeRecommendProductBatch(Long[] ids);
    
    PageResult<SmsHomeRecommendProduct> list(String productName, Integer recommendStatus,
                                              Integer pageNum, Integer pageSize);
    
    int updateRecommendStatus(Long[] ids, Integer recommendStatus);
    
    int updateSort(Long id, Integer sort);
}
