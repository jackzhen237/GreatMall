package org.example.mall_tiny01.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.config.ElasticsearchUtil;
import org.example.mall_tiny01.dto.BrandParam;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.PmsBrandMapper;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.service.BrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandServiceimpl implements BrandService {

    private static final Logger log = LoggerFactory.getLogger(BrandServiceimpl.class);

    @Autowired
    private PmsBrandMapper pmsBrandMapper;

    @Autowired
    private CacheQueryService cacheService;

    @Autowired
    private ElasticsearchUtil esUtil;

    @Override
    public PageResult<PmsBrand> list(String keyword, Integer pageNum, Integer pageSize, Integer showStatus) {
        if (keyword != null && !keyword.isEmpty()) {
            // 搜索链路：ES 拼音分词搜索替代 LIKE
            String cacheKey = "search:pms:brand:" + keyword
                    + ":page:" + pageNum + ":size:" + pageSize
                    + ":show:" + (showStatus != null ? showStatus : "null");

            String cached = cacheService.query(cacheKey, String.class, () -> {
                try {
                    List<PmsBrand> esResults = esUtil.searchPage("pms_brand", "name", keyword, pageNum, pageSize, PmsBrand.class);

                    // 内存中过滤 showStatus
                    if (showStatus != null) {
                        esResults = esResults.stream()
                                .filter(b -> showStatus.equals(b.getShowStatus()))
                                .collect(java.util.stream.Collectors.toList());
                    }

                    PageResult<PmsBrand> result = new PageResult<>();
                    result.setList(esResults);
                    result.setPageNum(pageNum);
                    result.setPageSize(pageSize);
                    long total = esUtil.count("pms_brand", "name", keyword);
                    result.setTotal(total);
                    result.setTotalPage((int) Math.ceil((double) total / pageSize));
                    return JSON.toJSONString(result);
                } catch (Exception e) {
                    log.error("ES 品牌搜索失败，降级到 MySQL. keyword={}", keyword, e);
                    return fallbackMysqlList(keyword, pageNum, pageSize, showStatus);
                }
            });

            if (cached == null || cached.isEmpty()) return null;
            return JSON.parseObject(cached, PageResult.class);
        }

        // 非搜索链路：MySQL 直查 + 多级缓存
        String cacheKey = "pms:brand:list:page:" + pageNum + ":size:" + pageSize
                + ":show:" + (showStatus != null ? showStatus : "null");

        String cached = cacheService.query(cacheKey, String.class, () -> {
            PageHelper.startPage(pageNum, pageSize);
            List<PmsBrand> list = pmsBrandMapper.list(null, showStatus);
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

    private String fallbackMysqlList(String keyword, Integer pageNum, Integer pageSize, Integer showStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<PmsBrand> list = pmsBrandMapper.list(keyword, showStatus);
        PageInfo<PmsBrand> pageInfo = new PageInfo<>(list);
        PageResult<PmsBrand> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        return JSON.toJSONString(result);
    }

    @Override
    public void deleteBrand(Integer id) {
        Long brandId = Long.valueOf(id);
        // 1. 删除 MySQL
        pmsBrandMapper.deleteByPrimaryKey(brandId);
        // 2. 删除 ES 文档
        deleteFromEs(brandId);
        // 3. 删除缓存
        cacheService.invalidate("pms:brand:" + brandId);
    }

    @Override
    public void deleteBrand(Integer[] ids) {
        if (ids != null && ids.length > 0) {
            for (Integer id : ids) {
                Long brandId = Long.valueOf(id);
                pmsBrandMapper.deleteByPrimaryKey(brandId);
                deleteFromEs(brandId);
                cacheService.invalidate("pms:brand:" + brandId);
            }
        }
    }

    @Override
    public void createBrand(BrandParam brandParam) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(brandParam, pmsBrand);
        pmsBrand.setProductCount(0);
        pmsBrand.setProductCommentCount(0);
        // 1. 写入 MySQL
        pmsBrandMapper.insertSelective(pmsBrand);
        // 2. 同步 ES
        syncToEs(pmsBrand.getId());
        // 3. 新增后无需删除缓存（新数据本来就不在缓存中）
    }

    @Override
    public void update(Long id, BrandParam brandParam) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(brandParam, pmsBrand);
        pmsBrand.setId(id);
        // 1. 更新 MySQL
        pmsBrandMapper.updateByPrimaryKeySelective(pmsBrand);
        // 2. 同步 ES
        syncToEs(id);
        // 3. 删除缓存
        cacheService.invalidate("pms:brand:" + id);
    }

    @Override
    public void updateShowStatus(Long[] ids, Integer showStatus) {
        pmsBrandMapper.updateShowStatus(ids, showStatus);
        for (Long id : ids) {
            syncExistingToEs(id);
            cacheService.invalidate("pms:brand:" + id);
        }
    }

    @Override
    public void updateFactoryStatus(Long[] ids, Integer factoryStatus) {
        pmsBrandMapper.updateFactoryStatus(ids, factoryStatus);
        for (Long id : ids) {
            syncExistingToEs(id);
            cacheService.invalidate("pms:brand:" + id);
        }
    }

    @Override
    public List<PmsBrand> listAll() {
        String cached = cacheService.query("pms:brand:list:all", String.class, () -> {
            List<PmsBrand> list = pmsBrandMapper.listAll();
            return JSON.toJSONString(list);
        });
        if (cached == null || cached.isEmpty()) return null;
        return JSON.parseArray(cached, PmsBrand.class);
    }

    @Override
    public PmsBrand getById(Long id) {
        return cacheService.query("pms:brand:" + id, PmsBrand.class,
                () -> pmsBrandMapper.selectByPrimaryKey(id));
    }

    // ======================== ES 同步辅助方法 ========================

    private void syncToEs(Long brandId) {
        try {
            PmsBrand brand = pmsBrandMapper.selectByPrimaryKey(brandId);
            if (brand != null) {
                esUtil.save("pms_brand", String.valueOf(brandId), brand);
            }
        } catch (Exception e) {
            log.error("ES 品牌同步失败（不阻断主流程）. brandId={}", brandId, e);
        }
    }

    private void syncExistingToEs(Long brandId) {
        syncToEs(brandId);
    }

    private void deleteFromEs(Long brandId) {
        try {
            esUtil.delete("pms_brand", String.valueOf(brandId));
        } catch (Exception e) {
            log.error("ES 品牌删除失败（不阻断主流程）. brandId={}", brandId, e);
        }
    }
}
