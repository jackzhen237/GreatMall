package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.SmsHomeBrandMapper;
import org.example.mall_tiny01.mbg.model.SmsHomeBrand;
import org.example.mall_tiny01.service.HomeBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeBrandServiceimpl implements HomeBrandService {
    @Autowired
    private SmsHomeBrandMapper smsHomeBrandMapper;

    @Override
    public int createHomeBrand(List<SmsHomeBrand> homeBrandList) {
        int count = 0;
        for (SmsHomeBrand homeBrand : homeBrandList) {
            count += smsHomeBrandMapper.insertSelective(homeBrand);
        }
        return count;
    }

    @Override
    public int deleteHomeBrandBatch(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += smsHomeBrandMapper.deleteByPrimaryKey(id);
        }
        return count;
    }

    @Override
    public PageResult<SmsHomeBrand> list(String brandName, Integer recommendStatus,
                                          Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<SmsHomeBrand> list = smsHomeBrandMapper.list(brandName, recommendStatus);
        
        PageInfo<SmsHomeBrand> pageInfo = new PageInfo<>(list);
        
        PageResult<SmsHomeBrand> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public int updateRecommendStatus(Long[] ids, Integer recommendStatus) {
        return smsHomeBrandMapper.updateRecommendStatus(ids, recommendStatus);
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeBrand homeBrand = new SmsHomeBrand();
        homeBrand.setId(id);
        homeBrand.setSort(sort);
        return smsHomeBrandMapper.updateByPrimaryKeySelective(homeBrand);
    }
}
