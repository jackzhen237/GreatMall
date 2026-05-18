package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.UmsResourceCategory;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface ResourceCategoryService {
    void createCategory(UmsResourceCategory category);

    void deleteCategory(Long id);

    List<UmsResourceCategory> listAll();

    void updateCategory(Long id, UmsResourceCategory category);
}
