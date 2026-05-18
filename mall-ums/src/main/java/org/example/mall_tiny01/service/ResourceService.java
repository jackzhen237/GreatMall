package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.UmsResource;

import java.util.List;

public interface ResourceService {
    void createResource(UmsResource resource);

    void deleteResource(Long id);

    PageResult<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageNum, Integer pageSize);

    List<UmsResource> listAll();

    void updateResource(Long id, UmsResource resource);

    UmsResource getResource(Long id);
}
