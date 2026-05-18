package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.SmsHomeRecommendProductMapper;
import org.example.mall_tiny01.mbg.model.SmsHomeRecommendProduct;
import org.example.mall_tiny01.service.HomeRecommendProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeRecommendProductServiceimpl implements HomeRecommendProductService {
    @Autowired
    private SmsHomeRecommendProductMapper smsHomeRecommendProductMapper;

    @Override
    public int createHomeRecommendProduct(List<SmsHomeRecommendProduct> recommendProductList) {
        int count = 0;
        for (SmsHomeRecommendProduct recommendProduct : recommendProductList) {
            count += smsHomeRecommendProductMapper.insertSelective(recommendProduct);
        }
        return count;
    }

    @Override
    public int deleteHomeRecommendProductBatch(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += smsHomeRecommendProductMapper.deleteByPrimaryKey(id);
        }
        return count;
    }

    @Override
    public PageResult<SmsHomeRecommendProduct> list(String productName, Integer recommendStatus,
                                                     Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<SmsHomeRecommendProduct> list = smsHomeRecommendProductMapper.list(productName, recommendStatus);
        
        PageInfo<SmsHomeRecommendProduct> pageInfo = new PageInfo<>(list);
        
        PageResult<SmsHomeRecommendProduct> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public int updateRecommendStatus(Long[] ids, Integer recommendStatus) {
        return smsHomeRecommendProductMapper.updateRecommendStatus(ids, recommendStatus);
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeRecommendProduct recommendProduct = new SmsHomeRecommendProduct();
        recommendProduct.setId(id);
        recommendProduct.setSort(sort);
        return smsHomeRecommendProductMapper.updateByPrimaryKeySelective(recommendProduct);
    }
}
