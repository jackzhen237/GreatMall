package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.mbg.model.PmsProduct;

public interface WebBrandService {
    PmsBrand getDetail(Long brandId);

    PageResult<PmsProduct> getProductList(Long brandId, Integer pageNum, Integer pageSize);

    PageResult<PmsBrand> getRecommendList(Integer pageNum, Integer pageSize);
}
