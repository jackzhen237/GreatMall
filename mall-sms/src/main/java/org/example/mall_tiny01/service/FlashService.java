package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.FlashPromotionDTO;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotion;
import org.springframework.stereotype.Service;

@Service
public interface FlashService {
    void save(FlashPromotionDTO flashPromotionDTO);

    void delete(Long id);

    PageResult<SmsFlashPromotion> list(String keyword, Integer pageNum, Integer pageSize);

    void updateStatus(Long id, Integer status);

    void update(Long id, FlashPromotionDTO flashPromotionDTO);

    SmsFlashPromotion getById(Long id);
}
