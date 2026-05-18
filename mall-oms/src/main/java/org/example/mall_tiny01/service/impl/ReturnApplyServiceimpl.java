package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.OmsOrderReturnApplyMapper;
import org.example.mall_tiny01.mbg.model.OmsOrderReturnApply;
import org.example.mall_tiny01.service.ReturnApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReturnApplyServiceimpl implements ReturnApplyService {
    @Autowired
    private OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;

    @Override
    public void deleteApply(List<Long> ids) {
        omsOrderReturnApplyMapper.deleteBatch(ids);
    }

    @Override
    public PageResult<OmsOrderReturnApply> list(Long id, Integer status, String receiverKeyword,
                                                String handleMan, String handleTime, String createTime,
                                                Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<OmsOrderReturnApply> list = omsOrderReturnApplyMapper.list(id, status, receiverKeyword, 
                                                                        handleMan, handleTime, createTime);

        PageInfo<OmsOrderReturnApply> pageInfo = new PageInfo<>(list);

        PageResult<OmsOrderReturnApply> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());

        return result;
    }

    @Override
    public void updateStatus(Long id, OmsOrderReturnApply returnApply) {
        returnApply.setId(id);
        omsOrderReturnApplyMapper.updateByPrimaryKeySelective(returnApply);
    }

    @Override
    public OmsOrderReturnApply getApply(Long id) {
        return omsOrderReturnApplyMapper.selectByPrimaryKey(id);
    }

    @Override
    public void create(OmsOrderReturnApply returnApply) {
        omsOrderReturnApplyMapper.insertSelective(returnApply);
    }
}
