package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.OmsOrderReturnReasonMapper;
import org.example.mall_tiny01.mbg.model.OmsOrderReturnReason;
import org.example.mall_tiny01.service.ReturnReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReturnReasonServiceimpl implements ReturnReasonService {
    @Autowired
    private OmsOrderReturnReasonMapper omsOrderReturnReasonMapper;

    @Override
    public void createReason(OmsOrderReturnReason reason) {
        if (reason.getCreateTime() == null) {
            reason.setCreateTime(new Date());
        }
        omsOrderReturnReasonMapper.insertSelective(reason);
    }

    @Override
    public void deleteReason(List<Long> ids) {
        omsOrderReturnReasonMapper.deleteBatch(ids);
    }

    @Override
    public PageResult<OmsOrderReturnReason> list(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<OmsOrderReturnReason> list = omsOrderReturnReasonMapper.list();

        PageInfo<OmsOrderReturnReason> pageInfo = new PageInfo<>(list);

        PageResult<OmsOrderReturnReason> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());

        return result;
    }

    @Override
    public void updateStatus(List<Long> ids, Integer status) {
        omsOrderReturnReasonMapper.updateStatus(ids, status);
    }

    @Override
    public void updateReason(Long id, OmsOrderReturnReason reason) {
        reason.setId(id);
        omsOrderReturnReasonMapper.updateByPrimaryKeySelective(reason);
    }

    @Override
    public OmsOrderReturnReason getReason(Long id) {
        return omsOrderReturnReasonMapper.selectByPrimaryKey(id);
    }
}