package org.example.mall_tiny01.dto;

import lombok.Data;
import org.example.mall_tiny01.mbg.model.UmsIntegrationConsumeSetting;
import org.example.mall_tiny01.mbg.model.UmsMemberReceiveAddress;

import java.util.Date;
import java.util.List;

@Data
public class ConfirmOrderResult {
    private List<CartPromotionItem> cartPromotionItemList;
    private List<SmsCouponHistoryDetail> couponHistoryDetailList;
    private UmsIntegrationConsumeSetting integrationConsumeSetting;
    private List<UmsMemberReceiveAddress> memberReceiveAddressList;
    private Integer memberIntegration;
    private CalcAmount calcAmount;
    private Long memberId;
    private String memberNickname;
    private Date createDate;
    private Date modifyDate;
    private Integer deleteStatus;
}