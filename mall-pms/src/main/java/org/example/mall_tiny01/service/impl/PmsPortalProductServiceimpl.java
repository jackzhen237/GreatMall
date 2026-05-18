package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.dto.PmsPortalProductDetail;
import org.example.mall_tiny01.dto.PmsProductCategoryNode;
import org.example.mall_tiny01.feign.SmsFeignClient;
import org.example.mall_tiny01.mbg.mapper.*;
import org.example.mall_tiny01.mbg.model.*;
import org.example.mall_tiny01.service.PmsPortalProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PmsPortalProductServiceimpl implements PmsPortalProductService {

    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;

    @Autowired
    private PmsProductMapper productMapper;

    @Autowired
    private PmsBrandMapper brandMapper;

    @Autowired
    private SmsFeignClient smsFeignClient;

    @Autowired
    private PmsProductAttributeMapper productAttributeMapper;

    @Autowired
    private PmsProductAttributeValueMapper productAttributeValueMapper;

    @Autowired
    private PmsProductFullReductionMapper productFullReductionMapper;

    @Autowired
    private PmsProductLadderMapper productLadderMapper;

    @Autowired
    private PmsSkuStockMapper skuStockMapper;

    @Autowired
    private CacheQueryService cacheService;

    @Override
    public List<PmsProductCategoryNode> categoryTreeList() {
        // 分类树是首页高频访问但变动不频繁的数据，用 Normal 级别缓存
        String cached = cacheService.query("pms:category:tree", String.class, () -> {
            List<PmsProductCategory> categoryList = productCategoryMapper.selectList();

            List<PmsProductCategory> showList = categoryList.stream()
                    .filter(category -> category.getShowStatus() != null && category.getShowStatus() == 1)
                    .sorted((c1, c2) -> {
                        if (c1.getSort() == null) return 1;
                        if (c2.getSort() == null) return -1;
                        return c2.getSort().compareTo(c1.getSort());
                    })
                    .collect(Collectors.toList());

            List<PmsProductCategoryNode> tree = buildCategoryTree(showList, 0L);
            return com.alibaba.fastjson.JSON.toJSONString(tree);
        });
        if (cached == null || cached.isEmpty()) return null;
        return com.alibaba.fastjson.JSON.parseArray(cached, PmsProductCategoryNode.class);
    }

    private List<PmsProductCategoryNode> buildCategoryTree(List<PmsProductCategory> categoryList, Long parentId) {
        List<PmsProductCategoryNode> nodeList = new ArrayList<>();
        
        List<PmsProductCategory> children = categoryList.stream()
                .filter(category -> category.getParentId() != null && category.getParentId().equals(parentId))
                .collect(Collectors.toList());
        
        for (PmsProductCategory category : children) {
            PmsProductCategoryNode node = new PmsProductCategoryNode();
            BeanUtils.copyProperties(category, node);
            
            List<PmsProductCategoryNode> childNodes = buildCategoryTree(categoryList, category.getId());
            if (!childNodes.isEmpty()) {
                node.setChildren(childNodes);
            }
            
            nodeList.add(node);
        }
        
        return nodeList;
    }

    @Override
    public PmsPortalProductDetail detail(Long id) {
        // 商品详情页是高频访问接口（秒杀/爆款），用 Hot 级别缓存（分布式锁防击穿）
        String cached = cacheService.queryHot("pms:product:detail:" + id, String.class, () -> {
            PmsPortalProductDetail result = buildDetail(id);
            return result == null ? null : com.alibaba.fastjson.JSON.toJSONString(result);
        });
        if (cached == null || cached.isEmpty()) return null;
        return com.alibaba.fastjson.JSON.parseObject(cached, PmsPortalProductDetail.class);
    }

    private PmsPortalProductDetail buildDetail(Long id) {
        PmsPortalProductDetail result = new PmsPortalProductDetail();

        PmsProduct product = productMapper.selectByPrimaryKey(id);
        if (product == null) {
            return null;
        }
        result.setProduct(product);
        
        PmsBrand brand = brandMapper.selectByPrimaryKey(product.getBrandId());
        result.setBrand(brand);
        
        List<SmsCoupon> couponList = getCouponList();
        result.setCouponList(couponList);
        
        List<PmsProductAttribute> allAttributes = productAttributeMapper.selectByCategoryId(product.getProductAttributeCategoryId());
        List<PmsProductAttribute> attributeList = allAttributes.stream()
                .filter(attr -> attr.getType() != null && attr.getType() == 1)
                .collect(Collectors.toList());
        result.setProductAttributeList(attributeList);
        
        List<PmsProductAttributeValue> allAttributeValues = getAllProductAttributeValue();
        List<PmsProductAttributeValue> attributeValueList = allAttributeValues.stream()
                .filter(value -> value.getProductId() != null && value.getProductId().equals(id))
                .collect(Collectors.toList());
        result.setProductAttributeValueList(attributeValueList);
        
        List<PmsProductFullReduction> allFullReductions = getAllProductFullReduction();
        List<PmsProductFullReduction> fullReductionList = allFullReductions.stream()
                .filter(reduction -> reduction.getProductId() != null && reduction.getProductId().equals(id))
                .collect(Collectors.toList());
        result.setProductFullReductionList(fullReductionList);
        
        List<PmsProductLadder> allLadders = getAllProductLadder();
        List<PmsProductLadder> ladderList = allLadders.stream()
                .filter(ladder -> ladder.getProductId() != null && ladder.getProductId().equals(id))
                .collect(Collectors.toList());
        result.setProductLadderList(ladderList);
        
        List<PmsSkuStock> skuStockList = skuStockMapper.list(id, null);
        result.setSkuStockList(skuStockList);
        
        return result;
    }

    private List<SmsCoupon> getCouponList() {
        List<SmsCoupon> allCoupons = smsFeignClient.listCoupons(null, 1).getData();
        Date now = new Date();
        
        return allCoupons.stream()
                .filter(coupon -> coupon.getPublishCount() != null && coupon.getPublishCount() > 0)
                .filter(coupon -> coupon.getStartTime() != null && coupon.getStartTime().before(now) 
                        || coupon.getStartTime() == null)
                .filter(coupon -> coupon.getEndTime() != null && coupon.getEndTime().after(now)
                        || coupon.getEndTime() == null)
                .collect(Collectors.toList());
    }

    private List<PmsProductAttributeValue> getAllProductAttributeValue() {
        return productAttributeValueMapper.listAll();
    }

    private List<PmsProductFullReduction> getAllProductFullReduction() {
        return productFullReductionMapper.listAll();
    }

    private List<PmsProductLadder> getAllProductLadder() {
        return productLadderMapper.listAll();
    }
}
