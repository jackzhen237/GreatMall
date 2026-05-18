package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.HomeContentResult;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.CmsSubject;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.mbg.model.PmsProductCategory;

import java.util.List;

public interface HomeService {
    HomeContentResult content();
    
    PageResult<PmsProduct> getHotProductList(Integer pageNum, Integer pageSize);
    
    PageResult<PmsProduct> getNewProductList(Integer pageNum, Integer pageSize);
    
    List<PmsProductCategory> getProductCateList(Long parentId);
    
    PageResult<PmsProduct> getRecommendProductList(Integer pageNum, Integer pageSize);
    
    PageResult<CmsSubject> getSubjectList(Long cateId, Integer pageNum, Integer pageSize);
}
