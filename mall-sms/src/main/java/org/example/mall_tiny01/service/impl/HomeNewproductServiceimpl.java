package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.SmsHomeNewProductMapper;
import org.example.mall_tiny01.mbg.model.SmsHomeNewProduct;
import org.example.mall_tiny01.service.HomeNewproductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeNewproductServiceimpl implements HomeNewproductService {
    @Autowired
    private SmsHomeNewProductMapper smsHomeNewProductMapper;

    @Override
    public int createHomeNewProduct(List<SmsHomeNewProduct> newProductList) {
        int count = 0;
        for (SmsHomeNewProduct newProduct : newProductList) {
            count += smsHomeNewProductMapper.insertSelective(newProduct);
        }
        return count;
    }

    @Override
    public int deleteHomeNewProductBatch(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += smsHomeNewProductMapper.deleteByPrimaryKey(id);
        }
        return count;
    }

    @Override
    public PageResult<SmsHomeNewProduct> list(String productName, Integer recommendStatus,
                                               Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<SmsHomeNewProduct> list = smsHomeNewProductMapper.list(productName, recommendStatus);
        
        PageInfo<SmsHomeNewProduct> pageInfo = new PageInfo<>(list);
        
        PageResult<SmsHomeNewProduct> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public int updateRecommendStatus(Long[] ids, Integer recommendStatus) {
        return smsHomeNewProductMapper.updateRecommendStatus(ids, recommendStatus);
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeNewProduct newProduct = new SmsHomeNewProduct();
        newProduct.setId(id);
        newProduct.setSort(sort);
        return smsHomeNewProductMapper.updateByPrimaryKeySelective(newProduct);
    }
}
