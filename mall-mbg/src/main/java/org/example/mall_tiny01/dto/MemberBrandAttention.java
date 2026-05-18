package org.example.mall_tiny01.dto;


import lombok.Data;

import java.util.Date;

@Data
public class MemberBrandAttention {
    private Long id;
    private Long brandId;
    private String brandName;
    private String brandLogo;
    private String brandCity;
    private Long memberId;
    private String memberNickname;
    private String memberIcon;
    private Date createTime;
}