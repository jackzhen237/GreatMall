package org.example.mall_tiny01.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.mbg.mapper.SmsHomeAdvertiseMapper;
import org.example.mall_tiny01.mbg.model.SmsHomeAdvertise;
import org.example.mall_tiny01.service.HomeAdvertiseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeAdvertiseServiceimpl implements HomeAdvertiseService {
    @Autowired
    private SmsHomeAdvertiseMapper smsHomeAdvertiseMapper;

    @Override
    public int createAdvertise(SmsHomeAdvertise advertise) {
        return smsHomeAdvertiseMapper.insertSelective(advertise);
    }

    @Override
    public int deleteAdvertiseBatch(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += smsHomeAdvertiseMapper.deleteByPrimaryKey(id);
        }
        return count;
    }

    @Override
    public PageResult<SmsHomeAdvertise> list(String name, Integer type, String endTime,
                                              Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        
        List<SmsHomeAdvertise> list = smsHomeAdvertiseMapper.list(name, type, endTime);
        
        PageInfo<SmsHomeAdvertise> pageInfo = new PageInfo<>(list);
        
        PageResult<SmsHomeAdvertise> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPage(pageInfo.getPages());
        
        return result;
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsHomeAdvertise advertise = new SmsHomeAdvertise();
        advertise.setId(id);
        advertise.setStatus(status);
        return smsHomeAdvertiseMapper.updateByPrimaryKeySelective(advertise);
    }

    @Override
    public int updateAdvertise(Long id, SmsHomeAdvertise advertise) {
        advertise.setId(id);
        return smsHomeAdvertiseMapper.updateByPrimaryKeySelective(advertise);
    }

    @Override
    public SmsHomeAdvertise getById(Long id) {
        return smsHomeAdvertiseMapper.selectByPrimaryKey(id);
    }
}
