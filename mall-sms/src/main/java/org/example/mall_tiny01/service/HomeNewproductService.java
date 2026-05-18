package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.SmsHomeNewProduct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HomeNewproductService {
    int createHomeNewProduct(List<SmsHomeNewProduct> newProductList);
    
    int deleteHomeNewProductBatch(Long[] ids);
    
    PageResult<SmsHomeNewProduct> list(String productName, Integer recommendStatus, 
                                        Integer pageNum, Integer pageSize);
    
    int updateRecommendStatus(Long[] ids, Integer recommendStatus);
    
    int updateSort(Long id, Integer sort);
}
