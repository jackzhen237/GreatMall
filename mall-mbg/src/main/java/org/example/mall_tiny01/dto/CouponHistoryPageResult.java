package org.example.mall_tiny01.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CouponHistoryPageResult {
    
    @ApiModelProperty("优惠券ID")
    private Long couponId;
    
    @ApiModelProperty("使用状态")
    private Integer useStatus;
    
    @ApiModelProperty("订单编号")
    private String orderSn;
    
    @ApiModelProperty("页码")
    private Integer pageNum;
    
    @ApiModelProperty("每页条数")
    private Integer pageSize;
}
