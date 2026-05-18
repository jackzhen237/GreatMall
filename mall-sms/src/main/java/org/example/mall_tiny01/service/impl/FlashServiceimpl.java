package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.FlashPromotionDTO;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.SmsFlashPromotionMapper;
import org.example.mall_tiny01.mbg.model.SmsFlashPromotion;
import org.example.mall_tiny01.service.FlashService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class FlashServiceimpl implements FlashService {
    @Autowired
    private SmsFlashPromotionMapper smsFlashPromotionMapper;

    @Override
    public void save(FlashPromotionDTO flashPromotionDTO) {
        SmsFlashPromotion flashPromotion = new SmsFlashPromotion();
        BeanUtils.copyProperties(flashPromotionDTO, flashPromotion);
        
        if (flashPromotionDTO.getStartDate() != null) {
            flashPromotion.setStartDate(Date.from(flashPromotionDTO.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (flashPromotionDTO.getEndDate() != null) {
            flashPromotion.setEndDate(Date.from(flashPromotionDTO.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (flashPromotionDTO.getCreateTime() != null) {
            flashPromotion.setCreateTime(Date.from(flashPromotionDTO.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        smsFlashPromotionMapper.insertSelective(flashPromotion);
    }

    @Override
    public void delete(Long id) {
        smsFlashPromotionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageResult<SmsFlashPromotion> list(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<SmsFlashPromotion> list = smsFlashPromotionMapper.list(keyword);
        
        PageInfo<SmsFlashPromotion> pageInfo = new PageInfo<>(list);
        
        PageResult<SmsFlashPromotion> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SmsFlashPromotion flashPromotion = new SmsFlashPromotion();
        flashPromotion.setId(id);
        flashPromotion.setStatus(status);
        smsFlashPromotionMapper.updateByPrimaryKeySelective(flashPromotion);
    }

    @Override
    public void update(Long id, FlashPromotionDTO flashPromotionDTO) {
        SmsFlashPromotion flashPromotion = new SmsFlashPromotion();
        BeanUtils.copyProperties(flashPromotionDTO, flashPromotion);
        flashPromotion.setId(id);
        
        if (flashPromotionDTO.getStartDate() != null) {
            flashPromotion.setStartDate(Date.from(flashPromotionDTO.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (flashPromotionDTO.getEndDate() != null) {
            flashPromotion.setEndDate(Date.from(flashPromotionDTO.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (flashPromotionDTO.getCreateTime() != null) {
            flashPromotion.setCreateTime(Date.from(flashPromotionDTO.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        smsFlashPromotionMapper.updateByPrimaryKeySelective(flashPromotion);
    }

    @Override
    public SmsFlashPromotion getById(Long id) {
        return smsFlashPromotionMapper.selectByPrimaryKey(id);
    }
}
