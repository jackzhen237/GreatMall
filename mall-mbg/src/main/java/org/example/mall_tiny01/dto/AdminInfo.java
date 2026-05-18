package org.example.mall_tiny01.dto;

import lombok.Data;
import org.example.mall_tiny01.mbg.model.UmsMenu;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdminInfo {
    private String name;              // 对应前端的 name
    private String avatar;            // 对应前端的 avatar
    private List<String> roles = new ArrayList<>();
    private List<UmsMenu> menus = new ArrayList<>();
}
