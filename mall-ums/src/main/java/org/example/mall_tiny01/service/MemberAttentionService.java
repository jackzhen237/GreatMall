package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.MemberBrandAttention;
import org.example.mall_tiny01.dto.PageResult;

public interface MemberAttentionService {
    void add(Long memberId, MemberBrandAttention attention);
    
    void clear(Long memberId);
    
    void delete(Long memberId, Long brandId);
    
    MemberBrandAttention detail(Long memberId, Long brandId);
    
    PageResult<MemberBrandAttention> list(Long memberId, Integer pageNum, Integer pageSize);
}
