package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.MemberBrandAttention;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.feign.PmsFeignClient;
import org.example.mall_tiny01.mbg.mapper.UmsMemberBrandAttentionMapper;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.mbg.model.UmsMemberBrandAttention;
import org.example.mall_tiny01.service.MemberAttentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MemberAttentionServiceimpl implements MemberAttentionService {
    
    @Autowired
    private PmsFeignClient pmsFeignClient;

    @Autowired
    private UmsMemberBrandAttentionMapper attentionMapper;

    @Override
    public void add(Long memberId, MemberBrandAttention attention) {
        if (attention.getBrandId() == null) {
            throw new RuntimeException("品牌ID不能为空");
        }
        
        PmsBrand existBrand = pmsFeignClient.getBrandDetail(attention.getBrandId()).getData();
        if (existBrand == null) {
            throw new RuntimeException("品牌不存在");
        }

        MemberBrandAttention record = new MemberBrandAttention();
        record.setMemberId(memberId);
        record.setBrandId(attention.getBrandId());
        record.setCreateTime(new Date());
        
        attentionMapper.insert(record);
    }

    @Override
    public void clear(Long memberId) {
        attentionMapper.deleteByMemberId(memberId);
    }

    @Override
    public void delete(Long memberId, Long brandId) {
        if (brandId == null) {
            throw new RuntimeException("品牌ID不能为空");
        }
        
        PmsBrand existBrand = pmsFeignClient.getBrandDetail(brandId).getData();
        if (existBrand == null) {
            throw new RuntimeException("品牌不存在");
        }

        attentionMapper.deleteByMemberIdAndBrandId(memberId, brandId);
    }

    @Override
    public MemberBrandAttention detail(Long memberId, Long brandId) {
        PmsBrand brand = pmsFeignClient.getBrandDetail(brandId).getData();
        if (brand == null) {
            throw new RuntimeException("品牌不存在");
        }

        MemberBrandAttention attention = new MemberBrandAttention();
        attention.setBrandId(brand.getId());
        attention.setBrandName(brand.getName());
        attention.setBrandLogo(brand.getLogo());
        attention.setBrandCity("");
        attention.setMemberId(memberId);
        attention.setCreateTime(new Date());
        
        return attention;
    }

    @Override
    public PageResult<MemberBrandAttention> list(Long memberId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<UmsMemberBrandAttention> attentionRecords = attentionMapper.selectByMemberId(memberId);
        PageInfo<UmsMemberBrandAttention> pageInfo = new PageInfo<>(attentionRecords);
        
        List<MemberBrandAttention> attentionList = new ArrayList<>();
        for (UmsMemberBrandAttention record : attentionRecords) {
            PmsBrand brand = pmsFeignClient.getBrandDetail(record.getBrandId()).getData();

            MemberBrandAttention attention = new MemberBrandAttention();
            attention.setId(record.getId());
            attention.setBrandId(record.getBrandId());
            if (brand != null) {
                attention.setBrandName(brand.getName());
                attention.setBrandLogo(brand.getLogo());
            }
            attention.setMemberId(memberId);
            attention.setCreateTime(record.getCreateTime());
            attentionList.add(attention);
        }
        
        PageResult<MemberBrandAttention> pageResult = new PageResult<>();
        pageResult.setList(attentionList);
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setTotalPage(pageInfo.getPages());
        
        return pageResult;
    }
}
