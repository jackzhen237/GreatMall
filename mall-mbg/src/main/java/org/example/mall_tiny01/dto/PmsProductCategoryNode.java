package org.example.mall_tiny01.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class PmsProductCategoryNode implements Serializable {

    private Long id;

    private Long parentId;

    private String name;

    private Integer level;

    private Integer productCount;

    private String productUnit;

    private Integer navStatus;

    private Integer showStatus;

    private Integer sort;

    private String icon;

    private String keywords;

    private String description;

    private List<PmsProductCategoryNode> children;
}