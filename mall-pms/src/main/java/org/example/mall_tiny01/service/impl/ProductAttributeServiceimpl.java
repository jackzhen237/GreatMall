package org.example.mall_tiny01.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.config.ElasticsearchUtil;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductAttributeCategoryItem;
import org.example.mall_tiny01.mbg.mapper.PmsProductAttributeCategoryMapper;
import org.example.mall_tiny01.mbg.mapper.PmsProductAttributeMapper;
import org.example.mall_tiny01.mbg.model.PmsProductAttribute;
import org.example.mall_tiny01.mbg.model.PmsProductAttributeCategory;
import org.example.mall_tiny01.service.ProductAttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductAttributeServiceimpl implements ProductAttributeService {

    private static final Logger log = LoggerFactory.getLogger(ProductAttributeServiceimpl.class);

    @Autowired
    private PmsProductAttributeCategoryMapper pmsProductAttributeCategoryMapper;
    @Autowired
    private PmsProductAttributeMapper pmsProductAttributeMapper;
    @Autowired
    private CacheQueryService cacheService;
    @Autowired
    private ElasticsearchUtil esUtil;

    // ======================== 属性分类 CRUD ========================

    @Override
    public void createCategory(PmsProductAttributeCategory productAttributeCategory) {
        // 1. 写入 MySQL
        pmsProductAttributeCategoryMapper.insertSelective(productAttributeCategory);

        // 2. 同步 ES
        syncAttrCategoryToEs(productAttributeCategory.getId());

        // 3. 删除缓存
        cacheService.invalidate("pms:attr:category:list:*");
    }

    @Override
    public void deleteCategory(Long id) {
        // 1. 删除 MySQL
        pmsProductAttributeCategoryMapper.deleteByPrimaryKey(id);

        // 2. 从 ES 删除
        deleteAttrCategoryFromEs(id);

        // 3. 删除缓存
        cacheService.invalidate("pms:attr:category:" + id);
        cacheService.invalidate("pms:attr:category:list:*");
    }

    @Override
    public void deleteCategory(List<Long> ids) {
        for (Long id : ids) {
            // 1. 删除 MySQL
            pmsProductAttributeCategoryMapper.deleteByPrimaryKey(id);
            // 2. 从 ES 删除
            deleteAttrCategoryFromEs(id);
            // 3. 删缓存
            cacheService.invalidate("pms:attr:category:" + id);
        }
        cacheService.invalidate("pms:attr:category:list:*");
    }

    // ======================== 属性 CRUD ========================

    @Override
    public void createAttribute(PmsProductAttribute productAttribute) {
        // 1. 写入 MySQL（insertSelective 会回填自增 ID）
        pmsProductAttributeMapper.insertSelective(productAttribute);

        // 2. 同步 ES
        syncAttributeToEs(productAttribute.getId());

        // 3. 删除缓存（属性变更，withAttr 缓存失效）
        cacheService.invalidate("pms:attr:category:withAttr");
    }

    @Override
    public void deleteAttribute(Long id) {
        // 1. 删除 MySQL
        pmsProductAttributeMapper.deleteByPrimaryKey(id);

        // 2. 从 ES 删除
        deleteAttributeFromEs(id);

        // 3. 删除缓存
        cacheService.invalidate("pms:attribute:" + id);
        cacheService.invalidate("pms:attr:category:withAttr");
    }

    @Override
    public void deleteAttribute(List<Long> ids) {
        for (Long id : ids) {
            // 1. 删除 MySQL
            pmsProductAttributeMapper.deleteByPrimaryKey(id);
            // 2. 从 ES 删除
            deleteAttributeFromEs(id);
            // 3. 删缓存
            cacheService.invalidate("pms:attribute:" + id);
        }
        cacheService.invalidate("pms:attr:category:withAttr");
    }

    // ======================== 查询 ========================

    @Override
    public PageResult<PmsProductAttribute> listAttribute(Long cid, Integer type, Integer pageNum, Integer pageSize) {
        String cacheKey = "pms:attribute:list:" + (cid != null ? cid : "null")
                + ":type:" + (type != null ? type : "null")
                + ":page:" + pageNum + ":size:" + pageSize;

        String cached = cacheService.query(cacheKey, String.class, () -> {
            PageHelper.startPage(pageNum, pageSize);
            List<PmsProductAttribute> list = pmsProductAttributeMapper.list(cid, type);
            PageInfo<PmsProductAttribute> pageInfo = new PageInfo<>(list);
            PageResult<PmsProductAttribute> result = new PageResult<>();
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

    // ======================== 更新 ========================

    @Override
    public void updateCategory(Long id, String name) {
        PmsProductAttributeCategory category = new PmsProductAttributeCategory();
        category.setId(id);
        category.setName(name);

        // 1. 更新 MySQL
        pmsProductAttributeCategoryMapper.updateByPrimaryKeySelective(category);

        // 2. 同步 ES（查出完整数据写入）
        syncAttrCategoryToEs(id);

        // 3. 删除缓存
        cacheService.invalidate("pms:attr:category:" + id);
        cacheService.invalidate("pms:attr:category:list:*");
    }

    @Override
    public void updateAttribute(PmsProductAttribute productAttribute) {
        // 1. 更新 MySQL
        pmsProductAttributeMapper.updateByPrimaryKeySelective(productAttribute);

        // 2. 同步 ES（查出完整数据写入）
        syncAttributeToEs(productAttribute.getId());

        // 3. 删除缓存
        cacheService.invalidate("pms:attribute:" + productAttribute.getId());
        cacheService.invalidate("pms:attr:category:withAttr");
    }

    // ======================== 单条查询 ========================

    @Override
    public PmsProductAttributeCategory getCategory(Long id) {
        return cacheService.query("pms:attr:category:" + id, PmsProductAttributeCategory.class,
                () -> pmsProductAttributeCategoryMapper.selectByPrimaryKey(id));
    }

    @Override
    public PmsProductAttribute getAttribute(Long id) {
        return cacheService.query("pms:attribute:" + id, PmsProductAttribute.class,
                () -> pmsProductAttributeMapper.selectByPrimaryKey(id));
    }

    // ======================== ES 同步辅助方法 ========================

    /**
     * 同步属性分类到 ES，查出完整数据后写入。
     */
    private void syncAttrCategoryToEs(Long id) {
        try {
            PmsProductAttributeCategory category = pmsProductAttributeCategoryMapper.selectByPrimaryKey(id);
            if (category != null) {
                esUtil.save("pms_product_attribute_category", String.valueOf(id), category);
            }
        } catch (Exception e) {
            log.error("ES 属性分类同步失败（不阻断主流程）. id={}", id, e);
        }
    }

    private void deleteAttrCategoryFromEs(Long id) {
        try {
            esUtil.delete("pms_product_attribute_category", String.valueOf(id));
        } catch (Exception e) {
            log.error("ES 属性分类删除失败（不阻断主流程）. id={}", id, e);
        }
    }

    /**
     * 同步属性到 ES，查出完整数据后写入。
     */
    private void syncAttributeToEs(Long id) {
        try {
            PmsProductAttribute attribute = pmsProductAttributeMapper.selectByPrimaryKey(id);
            if (attribute != null) {
                esUtil.save("pms_product_attribute", String.valueOf(id), attribute);
            }
        } catch (Exception e) {
            log.error("ES 属性同步失败（不阻断主流程）. id={}", id, e);
        }
    }

    private void deleteAttributeFromEs(Long id) {
        try {
            esUtil.delete("pms_product_attribute", String.valueOf(id));
        } catch (Exception e) {
            log.error("ES 属性删除失败（不阻断主流程）. id={}", id, e);
        }
    }
}
