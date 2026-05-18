package org.example.mall_tiny01.dto;

import org.example.mall_tiny01.mbg.model.UmsMenu;

import java.util.Date;
import java.util.List;

public class UmsMenuNode extends UmsMenu {
    private List<UmsMenuNode> children;

    public List<UmsMenuNode> getChildren() {
        return children;
    }

    public void setChildren(List<UmsMenuNode> children) {
        this.children = children;
    }
}
