package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductParam;
import org.example.mall_tiny01.dto.PmsProductResult;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PmsProductService {
    void createProduct(PmsProductParam productParam);
    PageResult<PmsProduct> list(Long brandId, String keyword, Long productCategoryId,
                                String productSn, Integer publishStatus, Integer verifyStatus,
                                Integer pageNum, Integer pageSize);


    List<PmsProduct> simpleList(String keyword);

    void updateDeleteStatus(List<Long> ids, Integer deleteStatus);

    void updateNewStatus(List<Long> ids, Integer newStatus);

    void updatePublishStatus(List<Long> ids, Integer publishStatus);

    void updateRecommendStatus(List<Long> ids, Integer recommendStatus);

    void updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail);

    void updateProduct(Long id, PmsProductParam productParam);

    PmsProductResult getUpdateInfo(Long id);
}
