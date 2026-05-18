package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.dto.SmsFlashPromotionSessionDetail;
import org.example.mall_tiny01.mbg.mapper.SmsFlashPromotionSessionMapper;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotionSession;
import org.example.mall_tiny01.service.FlashSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlashSessionServiceimpl implements FlashSessionService {
    
    @Autowired
    private SmsFlashPromotionSessionMapper smsFlashPromotionSessionMapper;

    @Override
    public void save(SmsFlashPromotionSession session) {
        smsFlashPromotionSessionMapper.insertSelective(session);
    }

    @Override
    public void delete(Long id) {
        smsFlashPromotionSessionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<SmsFlashPromotionSession> listAll() {
        return smsFlashPromotionSessionMapper.selectAll();
    }

    @Override
    public List<SmsFlashPromotionSessionDetail> selectList(Long flashPromotionId) {
        return smsFlashPromotionSessionMapper.selectList(flashPromotionId);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SmsFlashPromotionSession session = new SmsFlashPromotionSession();
        session.setId(id);
        session.setStatus(status);
        smsFlashPromotionSessionMapper.updateByPrimaryKeySelective(session);
    }

    @Override
    public void update(Long id, SmsFlashPromotionSession session) {
        session.setId(id);
        smsFlashPromotionSessionMapper.updateByPrimaryKeySelective(session);
    }

    @Override
    public SmsFlashPromotionSession getById(Long id) {
        return smsFlashPromotionSessionMapper.selectByPrimaryKey(id);
    }
}
