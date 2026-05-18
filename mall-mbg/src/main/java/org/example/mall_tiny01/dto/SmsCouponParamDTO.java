package org.example.mall_tiny01.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.example.mall_tiny01.mbg.model.SmsCouponProductCategoryRelation;
import org.example.mall_tiny01.mbg.model.SmsCouponProductRelation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class SmsCouponParamDTO {
    private Long id;
    
    private Integer type;
    
    private String name;
    
    private Integer platform;
    
    private Integer count;
    
    private BigDecimal amount;
    
    private Integer perLimit;
    
    private BigDecimal minPoint;
    
    // 1. 把 Date 改成 LocalDateTime
    private LocalDateTime startTime;
    
    // 2. 把 Date 改成 LocalDateTime
    private LocalDateTime endTime;
    
    private Integer useType;
    
    private String note;
    
    private Integer publishCount;
    
    private Integer useCount;
    
    private Integer receiveCount;
    
    // 3. 把 Date 改成 LocalDateTime
    private LocalDateTime enableTime;
    
    private String code;
    
    private Integer memberLevel;
    
    private List<SmsCouponProductCategoryRelation> productCategoryRelationList;
    
    private List<SmsCouponProductRelation> productRelationList;
}
