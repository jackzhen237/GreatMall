package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.SmsFlashPromotionProductRelationMapper;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotionProductRelation;
import org.example.mall_tiny01.service.FlashProductRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlashProductRelationServiceimpl implements FlashProductRelationService {
    @Autowired
    private SmsFlashPromotionProductRelationMapper smsFlashPromotionProductRelationMapper;

    @Override
    public void save(List<SmsFlashPromotionProductRelation> relationList) {
        for (SmsFlashPromotionProductRelation relation : relationList) {
            smsFlashPromotionProductRelationMapper.insertSelective(relation);
        }
    }

    @Override
    public void delete(Long id) {
        smsFlashPromotionProductRelationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageResult<SmsFlashPromotionProductRelation> list(Long flashPromotionId, Long flashPromotionSessionId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<SmsFlashPromotionProductRelation> list = smsFlashPromotionProductRelationMapper.list(flashPromotionId, flashPromotionSessionId);
        
        PageInfo<SmsFlashPromotionProductRelation> pageInfo = new PageInfo<>(list);
        
        PageResult<SmsFlashPromotionProductRelation> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public void update(Long id, SmsFlashPromotionProductRelation relation) {
        relation.setId(id);
        smsFlashPromotionProductRelationMapper.updateByPrimaryKeySelective(relation);
    }

    @Override
    public SmsFlashPromotionProductRelation getById(Long id) {
        return smsFlashPromotionProductRelationMapper.selectByPrimaryKey(id);
    }
}
