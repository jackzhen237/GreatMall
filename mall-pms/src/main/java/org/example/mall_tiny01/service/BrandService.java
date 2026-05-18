package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.BrandParam;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BrandService {
    void createBrand(BrandParam brandParam);

    void deleteBrand(Integer[] ids);

    void deleteBrand(Integer id);

    PageResult<PmsBrand> list(String keyword, Integer pageNum, Integer pageSize, Integer showStatus);

    List<PmsBrand> listAll();

    void updateFactoryStatus(Long[] ids, Integer factoryStatus);

    void updateShowStatus(Long[] ids, Integer showStatus);
    
    void update(Long id, BrandParam brandParam);

    PmsBrand getById(Long id);

}
