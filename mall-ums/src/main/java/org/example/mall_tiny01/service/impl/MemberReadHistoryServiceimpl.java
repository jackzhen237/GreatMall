package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.mbg.model.UmsMemberReadHistory;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.UmsMemberReadHistoryMapper;
import org.example.mall_tiny01.service.MemberReadHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class MemberReadHistoryServiceimpl implements MemberReadHistoryService {

    @Autowired
    private UmsMemberReadHistoryMapper readHistoryMapper;

    @Override
    public void create(UmsMemberReadHistory readHistory) {
        UmsMemberReadHistory record = new UmsMemberReadHistory();
        BeanUtils.copyProperties(readHistory, record);
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setCreateTime(new Date());
        readHistoryMapper.insertSelective(record);
    }

    @Override
    public void clear(Long memberId) {
        readHistoryMapper.deleteByMemberId(memberId);
    }

    @Override
    public void delete(Long memberId, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        readHistoryMapper.deleteByIds(ids);
    }

    @Override
    public PageResult<UmsMemberReadHistory> list(Long memberId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UmsMemberReadHistory> records = readHistoryMapper.selectByMemberId(memberId);
        PageInfo<UmsMemberReadHistory> pageInfo = new PageInfo<>(records);

        List<UmsMemberReadHistory> list = new ArrayList<>();
        for (UmsMemberReadHistory record : records) {
            UmsMemberReadHistory dto = new UmsMemberReadHistory();
            BeanUtils.copyProperties(record, dto);
            list.add(dto);
        }

        PageResult<UmsMemberReadHistory> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setTotalPage(pageInfo.getPages());
        return pageResult;
    }
}