package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.UmsMemberLevel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberLevelService {
    List<UmsMemberLevel> list(Integer defaultStatus);
}
