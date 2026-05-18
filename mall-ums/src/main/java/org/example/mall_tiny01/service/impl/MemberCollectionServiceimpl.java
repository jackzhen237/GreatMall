package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.mbg.model.UmsMemberProductCollection;
import org.example.mall_tiny01.mbg.mapper.UmsMemberProductCollectionMapper;
import org.example.mall_tiny01.service.MemberCollectionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MemberCollectionServiceimpl implements MemberCollectionService {

    @Autowired
    private UmsMemberProductCollectionMapper collectionMapper;

    @Override
    public void add(UmsMemberProductCollection productCollection) {
        if (productCollection.getProductId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }

        // 检查是否已收藏
        UmsMemberProductCollection exist = collectionMapper.selectByMemberIdAndProductId(
                productCollection.getMemberId(), productCollection.getProductId());
        if (exist != null) {
            throw new RuntimeException("该商品已在收藏夹中");
        }

        UmsMemberProductCollection record = new UmsMemberProductCollection();
        BeanUtils.copyProperties(productCollection, record);
        record.setCreateTime(new Date());
        collectionMapper.insertSelective(record);
    }

    @Override
    public void clear(Long memberId) {
        collectionMapper.deleteByMemberId(memberId);
    }

    @Override
    public void delete(Long memberId, Long productId) {
        if (productId == null) {
            throw new RuntimeException("商品ID不能为空");
        }
        collectionMapper.deleteByMemberIdAndProductId(memberId, productId);
    }

    @Override
    public UmsMemberProductCollection detail(Long memberId, Long productId) {
        UmsMemberProductCollection record = collectionMapper.selectByMemberIdAndProductId(memberId, productId);
        if (record == null) {
            throw new RuntimeException("收藏记录不存在");
        }
        UmsMemberProductCollection dto = new UmsMemberProductCollection();
        BeanUtils.copyProperties(record, dto);
        return dto;
    }

    @Override
    public List<UmsMemberProductCollection> list(Long memberId) {
        List<UmsMemberProductCollection> records = collectionMapper.selectByMemberId(memberId);
        List<UmsMemberProductCollection> list = new ArrayList<>();
        for (UmsMemberProductCollection record : records) {
            UmsMemberProductCollection dto = new UmsMemberProductCollection();
            BeanUtils.copyProperties(record, dto);
            list.add(dto);
        }
        return list;
    }
}