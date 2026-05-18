package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.OmsOrderReturnApply;

import java.util.List;

public interface ReturnApplyService {
    void deleteApply(List<Long> ids);

    PageResult<OmsOrderReturnApply> list(Long id, Integer status, String receiverKeyword, 
                                         String handleMan, String handleTime, String createTime, 
                                         Integer pageNum, Integer pageSize);

    void updateStatus(Long id, OmsOrderReturnApply returnApply);

    OmsOrderReturnApply getApply(Long id);
    
    void create(OmsOrderReturnApply returnApply);
}
