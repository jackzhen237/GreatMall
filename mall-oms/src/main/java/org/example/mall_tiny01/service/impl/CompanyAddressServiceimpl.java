package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.mbg.mapper.OmsCompanyAddressMapper;
import org.example.mall_tiny01.mbg.model.OmsCompanyAddress;
import org.example.mall_tiny01.service.CompanyAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyAddressServiceimpl implements CompanyAddressService {
    @Autowired
    private OmsCompanyAddressMapper omsCompanyAddressMapper;

    @Override
    public List<OmsCompanyAddress> list() {
        return omsCompanyAddressMapper.list();
    }
}
