package org.example.mall_tiny01.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductAttributeCategoryItem;
import org.example.mall_tiny01.mbg.mapper.PmsProductAttributeCategoryMapper;
import org.example.mall_tiny01.mbg.mapper.PmsProductAttributeMapper;
import org.example.mall_tiny01.mbg.model.PmsProductAttribute;
import org.example.mall_tiny01.mbg.model.PmsProductAttributeCategory;
import org.example.mall_tiny01.service.ProductAttributeCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductAttributeCategoryServiceimpl implements ProductAttributeCategoryService {
    @Autowired
    private PmsProductAttributeCategoryMapper pmsProductAttributeCategoryMapper;
    @Autowired
    private PmsProductAttributeMapper pmsProductAttributeMapper;
    @Autowired
    private CacheQueryService cacheService;

    @Override
    public void createCategory(PmsProductAttributeCategory productAttributeCategory) {
        pmsProductAttributeCategoryMapper.insertSelective(productAttributeCategory);
        cacheService.invalidate("pms:attr:category:list:*");
    }

    @Override
    public void deleteCategory(Long id) {
        pmsProductAttributeCategoryMapper.deleteByPrimaryKey(id);
        cacheService.invalidate("pms:attr:category:" + id);
        cacheService.invalidate("pms:attr:category:list:*");
        cacheService.invalidate("pms:attr:category:withAttr");
    }

    @Override
    public PageResult<PmsProductAttributeCategory> list(Integer pageNum, Integer pageSize) {
        String cacheKey = "pms:attr:category:list:page:" + pageNum + ":size:" + pageSize;

        String cached = cacheService.query(cacheKey, String.class, () -> {
            PageHelper.startPage(pageNum, pageSize);
            List<PmsProductAttributeCategory> list = pmsProductAttributeCategoryMapper.list();
            PageInfo<PmsProductAttributeCategory> pageInfo = new PageInfo<>(list);
            PageResult<PmsProductAttributeCategory> result = new PageResult<>();
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
    public List<PmsProductAttributeCategoryItem> listWithAttr() {
        String cacheKey = "pms:attr:category:withAttr";
        String cached = cacheService.query(cacheKey, String.class, () -> {
            List<PmsProductAttributeCategory> categoryList = pmsProductAttributeCategoryMapper.list();
            List<PmsProductAttributeCategoryItem> result = new ArrayList<>();
            for (PmsProductAttributeCategory category : categoryList) {
                PmsProductAttributeCategoryItem item = new PmsProductAttributeCategoryItem();
                BeanUtils.copyProperties(category, item);
                List<PmsProductAttribute> attributeList = pmsProductAttributeMapper.selectByCategoryId(category.getId());
                item.setProductAttributeList(attributeList);
                result.add(item);
            }
            return JSON.toJSONString(result);
        });

        if (cached == null || cached.isEmpty()) return null;
        return JSON.parseArray(cached, PmsProductAttributeCategoryItem.class);
    }

    @Override
    public PmsProductAttributeCategory getCategory(Long id) {
        return cacheService.query("pms:attr:category:" + id, PmsProductAttributeCategory.class,
                () -> pmsProductAttributeCategoryMapper.selectByPrimaryKey(id));
    }

    @Override
    public void updateCategory(Long id, String name) {
        PmsProductAttributeCategory category = new PmsProductAttributeCategory();
        category.setId(id);
        category.setName(name);
        pmsProductAttributeCategoryMapper.updateByPrimaryKeySelective(category);
        cacheService.invalidate("pms:attr:category:" + id);
        cacheService.invalidate("pms:attr:category:list:*");
    }
}
