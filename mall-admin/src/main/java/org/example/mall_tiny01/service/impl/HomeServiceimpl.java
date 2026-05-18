package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.config.CacheQueryService;
import org.example.mall_tiny01.dto.HomeContentResult;
import org.example.mall_tiny01.dto.HomeFlashPromotion;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.*;
import org.example.mall_tiny01.mbg.model.*;
import org.example.mall_tiny01.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeServiceimpl implements HomeService {
    @Autowired
    private SmsHomeAdvertiseMapper smsHomeAdvertiseMapper;

    @Autowired
    private PmsBrandMapper pmsBrandMapper;

    @Autowired
    private PmsProductMapper pmsProductMapper;

    @Autowired
    private CmsSubjectMapper cmsSubjectMapper;

    @Autowired
    private SmsFlashPromotionMapper smsFlashPromotionMapper;

    @Autowired
    private PmsProductCategoryMapper pmsProductCategoryMapper;

    @Autowired
    private CacheQueryService cacheService;

    @Override
    public HomeContentResult content() {
        return cacheService.query("home:content", HomeContentResult.class, () -> {
            return buildContent();
        });
    }

    private HomeContentResult buildContent() {
        HomeContentResult result = new HomeContentResult();

        result.setAdvertiseList(getHomeAdvertiseList());

        result.setBrandList(getBrandList(6));

        result.setHomeFlashPromotion(getHomeFlashPromotion());

        result.setHotProductList(getHotProductListInternal(6));

        result.setNewProductList(getNewProductListInternal(6));

        result.setSubjectList(getSubjectList(6, 0));

        return result;
    }

    @Override
    public PageResult<PmsProduct> getHotProductList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<PmsProduct> list = pmsProductMapper.getHotProductList();
        
        PageInfo<PmsProduct> pageInfo = new PageInfo<>(list);
        
        PageResult<PmsProduct> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public PageResult<PmsProduct> getNewProductList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<PmsProduct> list = pmsProductMapper.getNewProductList();
        
        PageInfo<PmsProduct> pageInfo = new PageInfo<>(list);
        
        PageResult<PmsProduct> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public List<PmsProductCategory> getProductCateList(Long parentId) {
        return pmsProductCategoryMapper.listByParentId(parentId);
    }

    @Override
    public PageResult<PmsProduct> getRecommendProductList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<PmsProduct> list = pmsProductMapper.getRecommendProductList();
        
        PageInfo<PmsProduct> pageInfo = new PageInfo<>(list);
        
        PageResult<PmsProduct> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public PageResult<CmsSubject> getSubjectList(Long cateId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<CmsSubject> list;
        if (cateId != null) {
            list = cmsSubjectMapper.listByCategoryId(cateId);
        } else {
            list = cmsSubjectMapper.list(null, null);
        }
        
        PageInfo<CmsSubject> pageInfo = new PageInfo<>(list);
        
        PageResult<CmsSubject> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    private List<SmsHomeAdvertise> getHomeAdvertiseList() {
        PageHelper.startPage(1, 5);
        return smsHomeAdvertiseMapper.list(null, 0, null);
    }

    private List<PmsBrand> getBrandList(int limit) {
        PageHelper.startPage(1, limit);
        return pmsBrandMapper.getRecommendList();
    }

    private HomeFlashPromotion getHomeFlashPromotion() {
        List<SmsFlashPromotion> flashPromotionList = smsFlashPromotionMapper.selectByStatus(1);
        if (flashPromotionList == null || flashPromotionList.isEmpty()) {
            return null;
        }

        HomeFlashPromotion homeFlashPromotion = new HomeFlashPromotion();
        SmsFlashPromotion flashPromotion = flashPromotionList.get(0);
        homeFlashPromotion.setId(flashPromotion.getId());
        homeFlashPromotion.setTitle(flashPromotion.getTitle());
        homeFlashPromotion.setStatus(flashPromotion.getStatus());
        homeFlashPromotion.setStartDate(flashPromotion.getStartDate().toString());
        homeFlashPromotion.setEndDate(flashPromotion.getEndDate().toString());

        return homeFlashPromotion;
    }

    private List<PmsProduct> getHotProductListInternal(int limit) {
        PageHelper.startPage(1, limit);
        return pmsProductMapper.getHotProductList();
    }

    private List<PmsProduct> getNewProductListInternal(int limit) {
        PageHelper.startPage(1, limit);
        return pmsProductMapper.getNewProductList();
    }

    private List<CmsSubject> getSubjectList(int limit, int type) {
        PageHelper.startPage(1, limit);
        return cmsSubjectMapper.list(null, type);
    }
}
