package org.example.mall_tiny01.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private Integer totalPage;
}
