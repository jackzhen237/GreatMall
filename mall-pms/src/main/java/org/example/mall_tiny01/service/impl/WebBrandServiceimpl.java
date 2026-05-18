package org.example.mall_tiny01.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.PmsBrandMapper;
import org.example.mall_tiny01.mbg.mapper.PmsProductMapper;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.service.WebBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebBrandServiceimpl implements WebBrandService {
    @Autowired
    private PmsBrandMapper pmsBrandMapper;

    @Autowired
    private PmsProductMapper pmsProductMapper;

    @Autowired
    private CacheQueryService cacheService;

    @Override
    public PmsBrand getDetail(Long brandId) {
        // 品牌详情（前端高频访问），Hot 级别缓存防击穿
        return cacheService.queryHot("pms:brand:" + brandId, PmsBrand.class,
                () -> pmsBrandMapper.selectByPrimaryKey(brandId));
    }

    @Override
    public PageResult<PmsProduct> getProductList(Long brandId, Integer pageNum, Integer pageSize) {
        String cacheKey = "pms:brand:product:" + brandId
                + ":page:" + pageNum + ":size:" + pageSize;

        String cached = cacheService.query(cacheKey, String.class, () -> {
            PageHelper.startPage(pageNum, pageSize);
            List<PmsProduct> list = pmsProductMapper.getProductListByBrand(brandId);
            PageInfo<PmsProduct> pageInfo = new PageInfo<>(list);
            PageResult<PmsProduct> result = new PageResult<>();
            result.setList(pageInfo.getList());
            result.setPageNum(pageInfo.getPageNum());
            result.setPageSize(pageInfo.getPageSize());
            result.setTotal(pageInfo.getTotal());
            result.setTotalPage(pageInfo.getPages());
            return JSON.toJSONString(result);
        });

        if (cached == null || cached.isEmpty()) return null;
        return JSON.parseObject(cached, PageResult.class);
    }

    @Override
    public PageResult<PmsBrand> getRecommendList(Integer pageNum, Integer pageSize) {
        // 推荐品牌列表，Normal 级别（访问频繁但变化不大）
        String cacheKey = "pms:brand:recommend:page:" + pageNum + ":size:" + pageSize;

        String cached = cacheService.query(cacheKey, String.class, () -> {
            PageHelper.startPage(pageNum, pageSize);
            List<PmsBrand> list = pmsBrandMapper.getRecommendList();
            PageInfo<PmsBrand> pageInfo = new PageInfo<>(list);
            PageResult<PmsBrand> result = new PageResult<>();
            result.setList(pageInfo.getList());
            result.setPageNum(pageInfo.getPageNum());
            result.setPageSize(pageInfo.getPageSize());
            result.setTotal(pageInfo.getTotal());
            result.setTotalPage(pageInfo.getPages());
            return JSON.toJSONString(result);
        });

        if (cached == null || cached.isEmpty()) return null;
        return JSON.parseObject(cached, PageResult.class);
    }
}
