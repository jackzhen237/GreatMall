package org.example.mall_tiny01.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.config.ElasticsearchUtil;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.PmsProductParam;
import org.example.mall_tiny01.dto.PmsProductResult;
import org.example.mall_tiny01.mbg.mapper.*;
import org.example.mall_tiny01.mbg.model.*;
import org.example.mall_tiny01.service.PmsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.seata.spring.annotation.GlobalTransactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PmsProductServiceimpl implements PmsProductService {

    private static final Logger log = LoggerFactory.getLogger(PmsProductServiceimpl.class);

    @Autowired
    private PmsProductMapper pmsProductMapper;

    @Autowired
    private CacheQueryService cacheService;

    @Autowired
    private ElasticsearchUtil esUtil;

    @Override
    public PageResult<PmsProduct> list(Long brandId, String keyword, Long productCategoryId,
                                       String productSn, Integer publishStatus, Integer verifyStatus,
                                       Integer pageNum, Integer pageSize) {
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasSn = productSn != null && !productSn.isEmpty();

        if (hasKeyword || hasSn) {
            // 搜索链路：走 ES 拼音分词搜索，替代 MySQL LIKE '%...%'
            String searchText = hasKeyword ? keyword : productSn;
            // ES 搜索缓存 Key 必须包含分页参数，防止不同页数据冲突
            String cacheKey = "search:pms:product:" + searchText
                    + ":page:" + pageNum + ":size:" + pageSize;

            String cached = cacheService.query(cacheKey, String.class, () -> {
                List<PmsProduct> esResults;
                try {
                    if (hasKeyword) {
                        // 多字段搜索：name + sub_title
                        List<String> fields = new ArrayList<>();
                        fields.add("name");
                        fields.add("subTitle");
                        esResults = esUtil.searchPage("pms_product", "name", keyword, pageNum, pageSize, PmsProduct.class);
                    } else {
                        esResults = esUtil.searchPage("pms_product", "productSn", productSn, pageNum, pageSize, PmsProduct.class);
                    }

                    // 内存中应用其他精确过滤条件（ES 返回的是分页后的结果，需要二次过滤）
                    List<PmsProduct> filtered = esResults.stream()
                            .filter(p -> brandId == null || (p.getBrandId() != null && p.getBrandId().equals(brandId)))
                            .filter(p -> productCategoryId == null || (p.getProductCategoryId() != null && p.getProductCategoryId().equals(productCategoryId)))
                            .filter(p -> publishStatus == null || (p.getPublishStatus() != null && p.getPublishStatus().equals(publishStatus)))
                            .filter(p -> verifyStatus == null || (p.getVerifyStatus() != null && p.getVerifyStatus().equals(verifyStatus)))
                            .collect(Collectors.toList());

                    PageResult<PmsProduct> result = new PageResult<>();
                    result.setList(filtered);
                    result.setPageNum(pageNum);
                    result.setPageSize(pageSize);
                    long total = esUtil.count("pms_product", "name", searchText);
                    result.setTotal(total);
                    result.setTotalPage((int) Math.ceil((double) total / pageSize));
                    return JSON.toJSONString(result);
                } catch (Exception e) {
                    log.error("ES 搜索失败，降级到 MySQL LIKE 查询. keyword={}", searchText, e);
                    // ES 不可用时降级回 MySQL
                    return fallbackMysqlList(brandId, keyword, productCategoryId, productSn,
                            publishStatus, verifyStatus, pageNum, pageSize);
                }
            });

            if (cached == null || cached.isEmpty()) return null;
            return JSON.parseObject(cached, PageResult.class);
        }

        // 非搜索链路：走 MySQL 直查，带多级缓存
        String cacheKey = "pms:product:list:"
                + (brandId != null ? brandId : "null") + ":"
                + (productCategoryId != null ? productCategoryId : "null") + ":"
                + (productSn != null ? productSn : "null") + ":"
                + (publishStatus != null ? publishStatus : "null") + ":"
                + (verifyStatus != null ? verifyStatus : "null") + ":"
                + "page:" + pageNum + ":size:" + pageSize;

        String cached = cacheService.query(cacheKey, String.class, () -> {
            PageHelper.startPage(pageNum, pageSize);
            List<PmsProduct> list = pmsProductMapper.list(
                    brandId, null, productCategoryId,
                    null, publishStatus, verifyStatus);
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

    /**
     * ES 不可用时的 MySQL LIKE 降级查询
     */
    private String fallbackMysqlList(Long brandId, String keyword, Long productCategoryId,
                                     String productSn, Integer publishStatus, Integer verifyStatus,
                                     Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PmsProduct> list = pmsProductMapper.list(
                brandId, keyword, productCategoryId,
                productSn, publishStatus, verifyStatus);
        PageInfo<PmsProduct> pageInfo = new PageInfo<>(list);
        PageResult<PmsProduct> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        return JSON.toJSONString(result);
    }

    @Override
    public void createProduct(PmsProductParam productParam) {
        if (productParam.getUnit() != null && productParam.getUnit().length() > 64) {
            productParam.setUnit(productParam.getUnit().substring(0, 64));
        }
        // 1. 先写 MySQL
        pmsProductMapper.insertSelective(productParam);

        // 2. 同步到 ES（失败不阻断主流程）
        syncToEs(productParam.getId());
    }

    @Override
    public List<PmsProduct> simpleList(String keyword) {
        // 搜索链路：ES 拼音分词搜索替代 LIKE
        String cacheKey = "search:pms:product:simple:" + keyword;

        String cached = cacheService.query(cacheKey, String.class, () -> {
            try {
                List<PmsProduct> esResults = esUtil.search("pms_product", "name", keyword, PmsProduct.class);
                return JSON.toJSONString(esResults);
            } catch (Exception e) {
                log.error("ES simpleList 搜索失败，降级到 MySQL. keyword={}", keyword, e);
                List<PmsProduct> list = pmsProductMapper.simpleList(keyword);
                return JSON.toJSONString(list);
            }
        });

        if (cached == null || cached.isEmpty()) return null;
        return JSON.parseArray(cached, PmsProduct.class);
    }

    @Override
    public void updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        // 1. 更新 MySQL
        pmsProductMapper.updateDeleteStatus(ids, deleteStatus);

        // 2. 同步 ES + 删除缓存
        for (Long id : ids) {
            syncExistingToEs(id);
            cacheService.invalidate("pms:product:detail:" + id);
            cacheService.invalidate("pms:product:update:" + id);
        }
    }

    @Override
    public void updateNewStatus(List<Long> ids, Integer newStatus) {
        pmsProductMapper.updateNewStatus(ids, newStatus);
        for (Long id : ids) {
            syncExistingToEs(id);
            cacheService.invalidate("pms:product:detail:" + id);
            cacheService.invalidate("pms:product:update:" + id);
        }
    }

    @Override
    public void updatePublishStatus(List<Long> ids, Integer publishStatus) {
        pmsProductMapper.updatePublishStatus(ids, publishStatus);
        for (Long id : ids) {
            syncExistingToEs(id);
            cacheService.invalidate("pms:product:detail:" + id);
            cacheService.invalidate("pms:product:update:" + id);
        }
    }

    @Override
    public void updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        pmsProductMapper.updateRecommendStatus(ids, recommendStatus);
        for (Long id : ids) {
            syncExistingToEs(id);
            cacheService.invalidate("pms:product:detail:" + id);
        }
    }

    @Override
    public void updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail) {
        pmsProductMapper.updateVerifyStatus(ids, verifyStatus, detail);
        for (Long id : ids) {
            syncExistingToEs(id);
            cacheService.invalidate("pms:product:detail:" + id);
            cacheService.invalidate("pms:product:update:" + id);
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, PmsProductParam productParam) {
        productParam.setId(id);
        // 1. 更新 MySQL
        pmsProductMapper.updateByPrimaryKeySelective(productParam);

        // 2. 同步 ES
        syncToEs(id);

        // 3. 删除缓存（删除而非更新，防止并发脏数据）
        cacheService.invalidate("pms:product:detail:" + id);
        cacheService.invalidate("pms:product:update:" + id);
    }

    @Override
    public PmsProductResult getUpdateInfo(Long id) {
        // 管理后台查询商品编辑信息，Normal 级别缓存
        String cached = cacheService.query("pms:product:update:" + id, String.class, () -> {
            PmsProduct product = pmsProductMapper.selectByPrimaryKey(id);
            if (product == null) return null;
            PmsProductResult result = new PmsProductResult();
            BeanUtils.copyProperties(product, result);
            return JSON.toJSONString(result);
        });
        if (cached == null || cached.isEmpty()) return null;
        return JSON.parseObject(cached, PmsProductResult.class);
    }

    // ======================== ES 同步辅助方法 ========================

    /**
     * 新增后同步：从 MySQL 查出最新数据写入 ES
     */
    private void syncToEs(Long productId) {
        try {
            PmsProduct product = pmsProductMapper.selectByPrimaryKey(productId);
            if (product != null) {
                esUtil.save("pms_product", String.valueOf(productId), product);
            }
        } catch (Exception e) {
            log.error("ES 同步失败（不阻断主流程）. productId={}", productId, e);
        }
    }

    /**
     * 状态更新后同步：只更新 ES 中已有的文档，不从 MySQL 全量查出（更轻量）
     */
    private void syncExistingToEs(Long productId) {
        try {
            PmsProduct product = pmsProductMapper.selectByPrimaryKey(productId);
            if (product != null) {
                esUtil.save("pms_product", String.valueOf(productId), product);
            }
        } catch (Exception e) {
            log.error("ES 同步失败（不阻断主流程）. productId={}", productId, e);
        }
    }
}
