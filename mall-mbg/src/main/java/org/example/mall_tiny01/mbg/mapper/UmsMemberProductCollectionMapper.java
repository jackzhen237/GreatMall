package org.example.mall_tiny01.mbg.mapper;


import org.example.mall_tiny01.mbg.model.UmsMemberProductCollection;
import java.util.List;

public interface UmsMemberProductCollectionMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberProductCollection row);

    int insertSelective(UmsMemberProductCollection row);

    UmsMemberProductCollection selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UmsMemberProductCollection row);

    int updateByPrimaryKey(UmsMemberProductCollection row);

    // 自定义方法
    int deleteByMemberId(Long memberId);

    int deleteByMemberIdAndProductId(Long memberId, Long productId);

    List<UmsMemberProductCollection> selectByMemberId(Long memberId);

    UmsMemberProductCollection selectByMemberIdAndProductId(Long memberId, Long productId);
}