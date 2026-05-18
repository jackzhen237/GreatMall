package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.UmsMemberReadHistory;
import org.example.mall_tiny01.dto.PageResult;
import java.util.List;

public interface MemberReadHistoryService {
    void create(UmsMemberReadHistory readHistory);
    void clear(Long memberId);
    void delete(Long memberId, List<String> ids);
    PageResult<UmsMemberReadHistory> list(Long memberId, Integer pageNum, Integer pageSize);
}