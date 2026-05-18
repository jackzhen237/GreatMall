package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductAttributeCategoryItem;
import org.example.mall_tiny01.mbg.model.PmsProductAttributeCategory;

import java.util.List;

public interface ProductAttributeCategoryService {
    void createCategory(PmsProductAttributeCategory productAttributeCategory);

    void deleteCategory(Long id);

    PageResult<PmsProductAttributeCategory> list(Integer pageNum, Integer pageSize);

    List<PmsProductAttributeCategoryItem> listWithAttr();

    PmsProductAttributeCategory getCategory(Long id);

    void updateCategory(Long id, String name);
}
