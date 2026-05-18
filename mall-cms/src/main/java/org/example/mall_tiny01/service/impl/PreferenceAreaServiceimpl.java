package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.mbg.mapper.CmsPrefrenceAreaMapper;
import org.example.mall_tiny01.mbg.model.CmsPrefrenceArea;
import org.example.mall_tiny01.service.PreferenceAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreferenceAreaServiceimpl implements PreferenceAreaService {
    @Autowired
    private CmsPrefrenceAreaMapper cmsPrefrenceAreaMapper;

    public List<CmsPrefrenceArea> listAll() {
        return cmsPrefrenceAreaMapper.selectAll();
    }
}
