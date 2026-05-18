package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.SmsHomeAdvertise;
import org.springframework.stereotype.Service;

@Service
public interface HomeAdvertiseService {
    int createAdvertise(SmsHomeAdvertise advertise);
    
    int deleteAdvertiseBatch(Long[] ids);
    
    PageResult<SmsHomeAdvertise> list(String name, Integer type, String endTime, 
                                      Integer pageNum, Integer pageSize);
    
    int updateStatus(Long id, Integer status);
    
    int updateAdvertise(Long id, SmsHomeAdvertise advertise);
    
    SmsHomeAdvertise getById(Long id);
}
