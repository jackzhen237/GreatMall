package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.SmsFlashPromotionSessionDetail;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotionSession;

import java.util.List;

public interface FlashSessionService {
    
    void save(SmsFlashPromotionSession session);

    void delete(Long id);

    List<SmsFlashPromotionSession> listAll();

    List<SmsFlashPromotionSessionDetail> selectList(Long flashPromotionId);

    void updateStatus(Long id, Integer status);

    void update(Long id, SmsFlashPromotionSession session);

    SmsFlashPromotionSession getById(Long id);
}
