package org.example.mall_tiny01.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.config.ElasticsearchUtil;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductCategoryParam;
import org.example.mall_tiny01.dto.PmsProductCategoryWithChildrenItem;
import org.example.mall_tiny01.mbg.mapper.PmsProductCategoryMapper;
import org.example.mall_tiny01.mbg.model.PmsProductCategory;
import org.example.mall_tiny01.service.ProductCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductCategoryServiceimpl implements ProductCategoryService {

    private static final Logger log = LoggerFactory.getLogger(ProductCategoryServiceimpl.class);

    @Autowired
    private PmsProductCategoryMapper pmsProductCategoryMapper;

    @Autowired
    private CacheQueryService cacheService;

    @Autowired
    private ElasticsearchUtil esUtil;

    @Override
    public void createCategory(PmsProductCategoryParam productCategoryParam) {
        PmsProductCategory productCategory = new PmsProductCategory();
        BeanUtils.copyProperties(productCategoryParam, productCategory);

        if (productCategory.getParentId() == null) {
            productCategory.setParentId(0L);
        }
        if (productCategory.getNavStatus() == null) {
            productCategory.setNavStatus(0);
        }
        if (productCategory.getShowStatus() == null) {
            productCategory.setShowStatus(0);
        }
        if (productCategory.getSort() == null) {
            productCategory.setSort(0);
        }
        if (productCategory.getParentId() == 0) {
            productCategory.setLevel(0);
        } else {
            PmsProductCategory parentCategory = pmsProductCategoryMapper.selectByPrimaryKey(productCategory.getParentId());
            if (parentCategory != null) {
                productCategory.setLevel(parentCategory.getLevel() + 1);
            } else {
                productCategory.setLevel(1);
            }
        }

        productCategory.setProductCount(0);

        // 1. 写入 MySQL（insertSelective 会回填自增 ID 到 productCategory 对象）
        pmsProductCategoryMapper.insertSelective(productCategory);

        // 2. 同步 ES（失败只记日志，不阻断主流程）
        syncToEs(productCategory.getId());

        // 3. 删除缓存
        cacheService.invalidate("pms:category:tree");
    }

    @Override
    public void deleteCategory(Long id) {
        // 1. 删除 MySQL
        pmsProductCategoryMapper.deleteByPrimaryKey(id);

        // 2. 从 ES 删除
        deleteFromEs(id);

        // 3. 删除缓存
        cacheService.invalidate("pms:category:" + id);
        cacheService.invalidate("pms:category:tree");
    }

    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {
        String cacheKey = "pms:category:list:withChildren";
        String cached = cacheService.query(cacheKey, String.class, () -> {
            List<PmsProductCategory> categoryList = pmsProductCategoryMapper.selectList();
            List<PmsProductCategoryWithChildrenItem> result = new ArrayList<>();
            for (PmsProductCategory category : categoryList) {
                if (category.getParentId() == 0) {
                    PmsProductCategoryWithChildrenItem item = new PmsProductCategoryWithChildrenItem();
                    BeanUtils.copyProperties(category, item);
                    item.setChildren(getChildren(category.getId(), categoryList));
                    result.add(item);
                }
            }
            return JSON.toJSONString(result);
        });
        if (cached == null || cached.isEmpty()) return null;
        return JSON.parseArray(cached, PmsProductCategoryWithChildrenItem.class);
    }

    @Override
    public PageResult<PmsProductCategory> listByParentId(Long parentId, Integer pageNum, Integer pageSize) {
        String cacheKey = "pms:category:children:" + (parentId != null ? parentId : "null")
                + ":page:" + pageNum + ":size:" + pageSize;

        String cached = cacheService.query(cacheKey, String.class, () -> {
            PageHelper.startPage(pageNum, pageSize);
            List<PmsProductCategory> list = pmsProductCategoryMapper.listByParentId(parentId);
            PageInfo<PmsProductCategory> pageInfo = new PageInfo<>(list);
            PageResult<PmsProductCategory> result = new PageResult<>();
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
    public void updateNavStatus(List<Long> ids, Integer navStatus) {
        // 1. 更新 MySQL
        pmsProductCategoryMapper.updateNavStatus(ids, navStatus);

        // 2. 同步 ES（逐个查出最新数据写入）
        for (Long id : ids) {
            syncToEs(id);
            cacheService.invalidate("pms:category:" + id);
        }
        // 3. 删除缓存
        cacheService.invalidate("pms:category:tree");
    }

    @Override
    public void updateShowStatus(List<Long> ids, Integer showStatus) {
        // 1. 更新 MySQL
        pmsProductCategoryMapper.updateShowStatus(ids, showStatus);

        // 2. 同步 ES
        for (Long id : ids) {
            syncToEs(id);
            cacheService.invalidate("pms:category:" + id);
        }
        // 3. 删除缓存
        cacheService.invalidate("pms:category:tree");
    }

    @Override
    public void updateCategory(Long id, PmsProductCategoryParam productCategoryParam) {
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setId(id);
        BeanUtils.copyProperties(productCategoryParam, productCategory);

        if (productCategory.getParentId() != null && !productCategory.getParentId().equals(0L)) {
            PmsProductCategory parentCategory = pmsProductCategoryMapper.selectByPrimaryKey(productCategory.getParentId());
            if (parentCategory != null) {
                productCategory.setLevel(parentCategory.getLevel() + 1);
            }
        } else if (productCategory.getParentId() != null && productCategory.getParentId().equals(0L)) {
            productCategory.setLevel(0);
        }

        // 1. 更新 MySQL
        pmsProductCategoryMapper.updateByPrimaryKeySelective(productCategory);

        // 2. 同步 ES（查出完整数据写入，因为 updateByPrimaryKeySelective 只更新了部分字段）
        syncToEs(id);

        // 3. 删除缓存
        cacheService.invalidate("pms:category:" + id);
        cacheService.invalidate("pms:category:tree");
    }

    @Override
    public PmsProductCategory getCategory(Long id) {
        return cacheService.query("pms:category:" + id, PmsProductCategory.class,
                () -> pmsProductCategoryMapper.selectByPrimaryKey(id));
    }

    private List<PmsProductCategory> getChildren(Long parentId, List<PmsProductCategory> allCategories) {
        List<PmsProductCategory> children = new ArrayList<>();
        for (PmsProductCategory category : allCategories) {
            if (category.getParentId().equals(parentId)) {
                children.add(category);
            }
        }
        return children;
    }

    // ======================== ES 同步辅助方法 ========================

    /**
     * 从 MySQL 查出最新完整数据，同步到 ES。
     * 失败只记日志，不往上抛异常，保证主流程不受影响。
     */
    private void syncToEs(Long categoryId) {
        try {
            PmsProductCategory category = pmsProductCategoryMapper.selectByPrimaryKey(categoryId);
            if (category != null) {
                esUtil.save("pms_category", String.valueOf(categoryId), category);
            }
        } catch (Exception e) {
            log.error("ES 分类同步失败（不阻断主流程）. categoryId={}", categoryId, e);
        }
    }

    /**
     * 从 ES 删除文档。
     */
    private void deleteFromEs(Long categoryId) {
        try {
            esUtil.delete("pms_category", String.valueOf(categoryId));
        } catch (Exception e) {
            log.error("ES 分类删除失败（不阻断主流程）. categoryId={}", categoryId, e);
        }
    }
}
