package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.UmsMemberProductCollection;

import java.util.List;

public interface MemberCollectionService {
    void add(UmsMemberProductCollection productCollection);

    void clear(Long memberId);

    void delete(Long memberId, Long productId);

    UmsMemberProductCollection detail(Long memberId, Long productId);

    List<UmsMemberProductCollection> list(Long memberId);
}