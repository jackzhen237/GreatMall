package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotionProductRelation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FlashProductRelationService {
    void save(List<SmsFlashPromotionProductRelation> relationList);

    void delete(Long id);

    PageResult<SmsFlashPromotionProductRelation> list(Long flashPromotionId, Long flashPromotionSessionId, Integer pageNum, Integer pageSize);

    void update(Long id, SmsFlashPromotionProductRelation relation);

    SmsFlashPromotionProductRelation getById(Long id);
}
