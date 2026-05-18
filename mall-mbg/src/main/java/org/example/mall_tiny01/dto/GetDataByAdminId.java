package org.example.mall_tiny01.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class GetDataByAdminId {
    /**
     * 角色ID (对应 int64)
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 后台用户数量 (对应 int32)
     */
    private Integer adminCount;

    /**
     * 创建时间
     * pattern: 指定输出格式为 yyyy-MM-dd HH:mm:ss
     * timezone: 指定时区为 GMT+8（北京时间），防止时间差 8 小时
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 排序 (对应 int32)
     */
    private Integer sort;

    /**
     * 启用状态：0->禁用；1->启用 (对应 int32)
     */
    private Integer status;
}
