package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.mbg.mapper.OmsOrderSettingMapper;
import org.example.mall_tiny01.mbg.model.OmsOrderSetting;
import org.example.mall_tiny01.service.OmsOrderSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OmsOrderSettingServiceimpl implements OmsOrderSettingService {

    @Autowired
    private OmsOrderSettingMapper omsOrderSettingMapper;

    @Override
    public OmsOrderSetting getItem(Long id) {
        return omsOrderSettingMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(Long id, OmsOrderSetting orderSetting) {
        orderSetting.setId(id);
        return omsOrderSettingMapper.updateByPrimaryKeySelective(orderSetting);
    }
}