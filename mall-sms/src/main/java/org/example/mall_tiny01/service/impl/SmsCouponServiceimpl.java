package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.SmsCouponParamDTO;
import org.example.mall_tiny01.mbg.mapper.SmsCouponMapper;
import org.example.mall_tiny01.mbg.mapper.SmsCouponProductCategoryRelationMapper;
import org.example.mall_tiny01.mbg.mapper.SmsCouponProductRelationMapper;
import org.example.mall_tiny01.mbg.model.SmsCoupon;
import org.example.mall_tiny01.mbg.model.SmsCouponProductCategoryRelation;
import org.example.mall_tiny01.mbg.model.SmsCouponProductRelation;
import org.example.mall_tiny01.service.SmsCouponService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.seata.spring.annotation.GlobalTransactional;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class SmsCouponServiceimpl implements SmsCouponService {
    @Autowired
    private SmsCouponProductCategoryRelationMapper smsCouponProductCategoryRelationMapper;
    @Autowired
    private SmsCouponProductRelationMapper smsCouponProductRelationMapper;
    @Autowired
    private SmsCouponMapper smsCouponMapper;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void save(SmsCouponParamDTO couponParam) {
        SmsCoupon coupon = new SmsCoupon();
        BeanUtils.copyProperties(couponParam, coupon);
        
        if (couponParam.getStartTime() != null) {
            coupon.setStartTime(Date.from(couponParam.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (couponParam.getEndTime() != null) {
            coupon.setEndTime(Date.from(couponParam.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (couponParam.getEnableTime() != null) {
            coupon.setEnableTime(Date.from(couponParam.getEnableTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        smsCouponMapper.insert(coupon);

        if (couponParam.getProductCategoryRelationList() != null && !couponParam.getProductCategoryRelationList().isEmpty()) {
            for (SmsCouponProductCategoryRelation categoryRelation : couponParam.getProductCategoryRelationList()) {
                smsCouponProductCategoryRelationMapper.insertSelective(categoryRelation);
            }
        }

        if (couponParam.getProductRelationList() != null && !couponParam.getProductRelationList().isEmpty()) {
            for (SmsCouponProductRelation productRelation : couponParam.getProductRelationList()) {
                smsCouponProductRelationMapper.insertSelective(productRelation);
            }
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        smsCouponProductCategoryRelationMapper.deleteByCouponId(id);
        smsCouponProductRelationMapper.deleteByCouponId(id);
        smsCouponMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageResult<SmsCoupon> list(String name, Integer type, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<SmsCoupon> list = smsCouponMapper.list(name, type);
        
        PageInfo<SmsCoupon> pageInfo = new PageInfo<>(list);
        
        PageResult<SmsCoupon> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void update(Long id, SmsCouponParamDTO couponParam) {
        SmsCoupon coupon = new SmsCoupon();
        BeanUtils.copyProperties(couponParam, coupon);
        coupon.setId(id);
        
        if (couponParam.getStartTime() != null) {
            coupon.setStartTime(Date.from(couponParam.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (couponParam.getEndTime() != null) {
            coupon.setEndTime(Date.from(couponParam.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (couponParam.getEnableTime() != null) {
            coupon.setEnableTime(Date.from(couponParam.getEnableTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        smsCouponMapper.updateByPrimaryKeySelective(coupon);
        
        smsCouponProductCategoryRelationMapper.deleteByCouponId(id);
        if (couponParam.getProductCategoryRelationList() != null && !couponParam.getProductCategoryRelationList().isEmpty()) {
            for (SmsCouponProductCategoryRelation categoryRelation : couponParam.getProductCategoryRelationList()) {
                categoryRelation.setId(null);
                categoryRelation.setCouponId(id);
                smsCouponProductCategoryRelationMapper.insertSelective(categoryRelation);
            }
        }
        
        smsCouponProductRelationMapper.deleteByCouponId(id);
        if (couponParam.getProductRelationList() != null && !couponParam.getProductRelationList().isEmpty()) {
            for (SmsCouponProductRelation productRelation : couponParam.getProductRelationList()) {
                productRelation.setId(null);
                productRelation.setCouponId(id);
                smsCouponProductRelationMapper.insertSelective(productRelation);
            }
        }
    }

    @Override
    public List<SmsCoupon> listAll(String name, Integer type) {
        return smsCouponMapper.list(name, type);
    }

    @Override
    public SmsCouponParamDTO getById(Long id) {
        SmsCoupon coupon = smsCouponMapper.selectByPrimaryKey(id);
        
        SmsCouponParamDTO couponParam = new SmsCouponParamDTO();
        BeanUtils.copyProperties(coupon, couponParam);
        
        SmsCouponProductCategoryRelation categoryExample = new SmsCouponProductCategoryRelation();
        categoryExample.setCouponId(id);
        List<SmsCouponProductCategoryRelation> categoryList = smsCouponProductCategoryRelationMapper.selectByCouponId(id);
        couponParam.setProductCategoryRelationList(categoryList);
        
        List<SmsCouponProductRelation> productList = smsCouponProductRelationMapper.selectByCouponId(id);
        couponParam.setProductRelationList(productList);
        
        return couponParam;
    }

}
