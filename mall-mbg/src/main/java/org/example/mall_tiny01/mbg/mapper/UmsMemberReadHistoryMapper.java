package org.example.mall_tiny01.mbg.mapper;


import org.example.mall_tiny01.mbg.model.UmsMemberReadHistory;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UmsMemberReadHistoryMapper {
    int insert(UmsMemberReadHistory row);
    int insertSelective(UmsMemberReadHistory row);
    int deleteByPrimaryKey(String id);
    UmsMemberReadHistory selectByPrimaryKey(String id);
    int updateByPrimaryKeySelective(UmsMemberReadHistory row);

    // 自定义方法
    int deleteByMemberId(Long memberId);
    int deleteByIds(@Param("ids") List<String> ids);
    List<UmsMemberReadHistory> selectByMemberId(Long memberId);
}