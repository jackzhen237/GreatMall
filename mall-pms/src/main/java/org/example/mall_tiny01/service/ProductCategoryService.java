package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductCategoryParam;
import org.example.mall_tiny01.dto.PmsProductCategoryWithChildrenItem;
import org.example.mall_tiny01.mbg.model.PmsProductCategory;

import java.util.List;

public interface ProductCategoryService {
    void createCategory(PmsProductCategoryParam productCategoryParam);

    void deleteCategory(Long id);

    List<PmsProductCategoryWithChildrenItem> listWithChildren();

    PageResult<PmsProductCategory> listByParentId(Long parentId, Integer pageNum, Integer pageSize);

    void updateNavStatus(List<Long> ids, Integer navStatus);

    void updateShowStatus(List<Long> ids, Integer showStatus);

    void updateCategory(Long id, PmsProductCategoryParam productCategoryParam);

    PmsProductCategory getCategory(Long id);
}
