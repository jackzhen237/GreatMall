package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.CmsSubject;

import java.util.List;

public interface SubjectService {
    PageResult<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize);

    List<CmsSubject> listAll();
}
