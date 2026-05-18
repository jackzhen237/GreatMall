package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductAttributeCategoryItem;
import org.example.mall_tiny01.mbg.model.PmsProductAttribute;
import org.example.mall_tiny01.mbg.model.PmsProductAttributeCategory;

import java.util.List;

public interface ProductAttributeService {
    void createCategory(PmsProductAttributeCategory productAttributeCategory);

    void deleteCategory(Long id);

    void deleteCategory(List<Long> ids);

    PageResult<PmsProductAttributeCategory> list(Integer pageNum, Integer pageSize);

    List<PmsProductAttributeCategoryItem> listWithAttr();

    void updateCategory(Long id, String name);

    PmsProductAttributeCategory getCategory(Long id);

    void createAttribute(PmsProductAttribute productAttribute);

    void deleteAttribute(Long id);

    void deleteAttribute(List<Long> ids);

    void updateAttribute(PmsProductAttribute productAttribute);

    PmsProductAttribute getAttribute(Long id);

    PageResult<PmsProductAttribute> listAttribute(Long cid, Integer type, Integer pageNum, Integer pageSize);

}
