package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.CmsPrefrenceArea;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PreferenceAreaService {
    List<CmsPrefrenceArea> listAll();
}
