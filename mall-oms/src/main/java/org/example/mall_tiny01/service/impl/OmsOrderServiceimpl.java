package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.OmsOrderDeliveryParam;
import org.example.mall_tiny01.dto.OmsOrderDetail;
import org.example.mall_tiny01.dto.OmsOrderMoneyParam;
import org.example.mall_tiny01.dto.OmsOrderReceiverInfoParam;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.OmsOrderItemMapper;
import org.example.mall_tiny01.mbg.mapper.OmsOrderMapper;
import org.example.mall_tiny01.mbg.mapper.OmsOrderOperateHistoryMapper;
import org.example.mall_tiny01.mbg.model.OmsOrder;
import org.example.mall_tiny01.mbg.model.OmsOrderItem;
import org.example.mall_tiny01.mbg.model.OmsOrderOperateHistory;
import org.example.mall_tiny01.service.OmsOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OmsOrderServiceimpl implements OmsOrderService {
    
    @Autowired
    private OmsOrderMapper omsOrderMapper;
    
    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;
    
    @Autowired
    private OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;

    @Override
    public PageResult<OmsOrder> list(String orderSn, Integer status, Integer orderType,
                                     Integer sourceType, String receiverKeyword,
                                     String createTime, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<OmsOrder> list = omsOrderMapper.list(orderSn, status, orderType,
                                                   sourceType, null, receiverKeyword, createTime);
        
        PageInfo<OmsOrder> pageInfo = new PageInfo<>(list);
        
        PageResult<OmsOrder> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public int delete(List<Long> ids) {
        return omsOrderMapper.deleteByIds(ids);
    }

    @Override
    public int close(List<Long> ids, String note) {
        return omsOrderMapper.closeByIds(ids, note);
    }

    @Override
    public int delivery(List<OmsOrderDeliveryParam> deliveryParams) {
        int count = 0;
        for (OmsOrderDeliveryParam param : deliveryParams) {
            OmsOrder order = new OmsOrder();
            order.setId(param.getOrderId());
            order.setDeliveryCompany(param.getDeliveryCompany());
            order.setDeliverySn(param.getDeliverySn());
            order.setStatus(2);
            order.setDeliveryTime(new Date());
            count += omsOrderMapper.updateByPrimaryKeySelective(order);
        }
        return count;
    }

    @Override
    public int updateMoneyInfo(OmsOrderMoneyParam moneyParam) {
        OmsOrder order = new OmsOrder();
        order.setId(moneyParam.getOrderId());
        order.setFreightAmount(moneyParam.getFreightAmount());
        order.setDiscountAmount(moneyParam.getDiscountAmount());
        order.setStatus(moneyParam.getStatus());
        return omsOrderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public int updateReceiverInfo(OmsOrderReceiverInfoParam receiverInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(receiverInfoParam.getOrderId());
        order.setReceiverName(receiverInfoParam.getReceiverName());
        order.setReceiverPhone(receiverInfoParam.getReceiverPhone());
        order.setReceiverPostCode(receiverInfoParam.getReceiverPostCode());
        order.setReceiverProvince(receiverInfoParam.getReceiverProvince());
        order.setReceiverCity(receiverInfoParam.getReceiverCity());
        order.setReceiverRegion(receiverInfoParam.getReceiverRegion());
        order.setReceiverDetailAddress(receiverInfoParam.getReceiverDetailAddress());
        order.setStatus(receiverInfoParam.getStatus());
        return omsOrderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public int updateNote(Long id, String note, Integer status) {
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setNote(note);
        order.setStatus(status);
        return omsOrderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public OmsOrderDetail getDetail(Long id) {
        OmsOrder order = omsOrderMapper.selectByPrimaryKey(id);
        
        if (order == null) {
            return null;
        }
        
        OmsOrderDetail detail = new OmsOrderDetail();
        BeanUtils.copyProperties(order, detail);
        
        List<OmsOrderItem> orderItemList = omsOrderItemMapper.listByOrderId(id);
        detail.setOrderItemList(orderItemList);
        
        List<OmsOrderOperateHistory> historyList = omsOrderOperateHistoryMapper.listByOrderId(id);
        detail.setHistoryList(historyList);
        
        return detail;
    }
}
