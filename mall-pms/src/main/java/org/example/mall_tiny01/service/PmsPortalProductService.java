package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PmsPortalProductDetail;
import org.example.mall_tiny01.dto.PmsProductCategoryNode;

import java.util.List;

public interface PmsPortalProductService {
    
    List<PmsProductCategoryNode> categoryTreeList();
    
    PmsPortalProductDetail detail(Long id);
}
