package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.OmsOrderReturnReason;

import java.util.List;

public interface ReturnReasonService {
    void createReason(OmsOrderReturnReason reason);

    void deleteReason(List<Long> ids);

    PageResult<OmsOrderReturnReason> list(Integer pageNum, Integer pageSize);

    void updateStatus(List<Long> ids, Integer status);

    void updateReason(Long id, OmsOrderReturnReason reason);

    OmsOrderReturnReason getReason(Long id);
}
