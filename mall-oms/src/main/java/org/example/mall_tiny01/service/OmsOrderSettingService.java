package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.OmsOrderSetting;

public interface OmsOrderSettingService {

    OmsOrderSetting getItem(Long id);

    int update(Long id, OmsOrderSetting orderSetting);
}
