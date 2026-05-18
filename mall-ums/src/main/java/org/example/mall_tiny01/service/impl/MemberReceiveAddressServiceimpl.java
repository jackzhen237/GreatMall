package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.mbg.mapper.UmsMemberReceiveAddressMapper;
import org.example.mall_tiny01.mbg.model.UmsMemberReceiveAddress;
import org.example.mall_tiny01.service.MemberReceiveAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberReceiveAddressServiceimpl implements MemberReceiveAddressService {
    @Autowired
    private UmsMemberReceiveAddressMapper addressMapper;

    @Override
    public int add(UmsMemberReceiveAddress address) {
        return addressMapper.insertSelective(address);
    }

    @Override
    public int delete(Long id) {
        return addressMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<UmsMemberReceiveAddress> list(Long memberId) {
        return addressMapper.listByMemberId(memberId);
    }

    @Override
    public int update(Long id, UmsMemberReceiveAddress address) {
        address.setId(id);
        return addressMapper.updateByPrimaryKeySelective(address);
    }

    @Override
    public UmsMemberReceiveAddress getDetail(Long id) {
        return addressMapper.selectByPrimaryKey(id);
    }
}
