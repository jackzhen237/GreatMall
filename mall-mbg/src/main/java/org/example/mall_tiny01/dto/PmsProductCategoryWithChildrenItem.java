package org.example.mall_tiny01.dto;

import org.example.mall_tiny01.mbg.model.PmsProductCategory;

import java.util.List;

public class PmsProductCategoryWithChildrenItem extends PmsProductCategory {
    private List<PmsProductCategory> children;

    public List<PmsProductCategory> getChildren() {
        return children;
    }

    public void setChildren(List<PmsProductCategory> children) {
        this.children = children;
    }
}