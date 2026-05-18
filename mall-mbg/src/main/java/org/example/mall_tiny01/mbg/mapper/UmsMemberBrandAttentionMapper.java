package org.example.mall_tiny01.mbg.mapper;


import org.example.mall_tiny01.dto.MemberBrandAttention;
import org.example.mall_tiny01.mbg.model.UmsMemberBrandAttention;

import java.util.List;

public interface UmsMemberBrandAttentionMapper {
    int insert(MemberBrandAttention row);

    int insertSelective(MemberBrandAttention row);

    MemberBrandAttention selectByPrimaryKey(Long id);

    int deleteByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MemberBrandAttention row);

    int updateByPrimaryKey(MemberBrandAttention row);

    int deleteByMemberId(Long memberId);

    int deleteByMemberIdAndBrandId(Long memberId, Long brandId);

    List<UmsMemberBrandAttention> selectByMemberId(Long memberId);
}