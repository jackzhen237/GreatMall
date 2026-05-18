package org.example.mall_tiny01.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.example.mall_tiny01.mbg.model.OmsOrder;
import org.example.mall_tiny01.mbg.model.OmsOrderItem;
import org.example.mall_tiny01.mbg.model.OmsOrderOperateHistory;

import java.util.List;

@Data
public class OmsOrderDetail extends OmsOrder {

    @ApiModelProperty("订单商品列表")
    private List<OmsOrderItem> orderItemList;

    @ApiModelProperty("订单操作记录列表")
    private List<OmsOrderOperateHistory> historyList;
}