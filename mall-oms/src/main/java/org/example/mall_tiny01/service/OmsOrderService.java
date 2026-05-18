package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.*;
import org.example.mall_tiny01.mbg.model.OmsOrder;

import java.util.List;

public interface OmsOrderService {
    
    PageResult<OmsOrder> list(String orderSn, Integer status, Integer orderType,
                              Integer sourceType, String receiverKeyword, 
                              String createTime, Integer pageNum, Integer pageSize);
    
    int delete(List<Long> ids);
    
    int close(List<Long> ids, String note);
    
    int delivery(List<OmsOrderDeliveryParam> deliveryParams);
    
    int updateMoneyInfo(OmsOrderMoneyParam moneyParam);
    
    int updateReceiverInfo(OmsOrderReceiverInfoParam receiverInfoParam);
    
    int updateNote(Long id, String note, Integer status);
    
    OmsOrderDetail getDetail(Long id);
}
