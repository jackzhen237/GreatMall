package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.mbg.mapper.UmsMemberLevelMapper;
import org.example.mall_tiny01.mbg.model.UmsMemberLevel;
import org.example.mall_tiny01.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberLevelServiceimpl implements MemberLevelService {
    @Autowired
    private UmsMemberLevelMapper umsMemberLevelMapper;

    @Override
    public List<UmsMemberLevel> list(Integer defaultStatus) {
        return umsMemberLevelMapper.list(defaultStatus);
    }
}
