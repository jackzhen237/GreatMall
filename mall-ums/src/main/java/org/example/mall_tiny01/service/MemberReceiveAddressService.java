package org.example.mall_tiny01.service;

import org.example.mall_tiny01.mbg.model.UmsMemberReceiveAddress;

import java.util.List;

public interface MemberReceiveAddressService {
    int add(UmsMemberReceiveAddress address);

    int delete(Long id);

    List<UmsMemberReceiveAddress> list(Long memberId);
    
    int update(Long id, UmsMemberReceiveAddress address);

    UmsMemberReceiveAddress getDetail(Long id);
}
