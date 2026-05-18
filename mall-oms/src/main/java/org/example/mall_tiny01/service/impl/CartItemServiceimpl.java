package org.example.mall_tiny01.service.impl;

import org.example.mall_tiny01.dto.CartProduct;
import org.example.mall_tiny01.dto.CartPromotionItem;
import org.example.mall_tiny01.feign.PmsFeignClient;
import org.example.mall_tiny01.feign.UmsFeignClient;
import org.example.mall_tiny01.mbg.mapper.OmsCartItemMapper;
import org.example.mall_tiny01.mbg.model.OmsCartItem;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CartItemServiceimpl implements CartItemService {
    @Autowired
    private OmsCartItemMapper omsCartItemMapper;

    @Autowired
    private PmsFeignClient pmsFeignClient;

    @Autowired
    private UmsFeignClient umsFeignClient;

    @Override
    public void add(OmsCartItem cartItem) {
        if (cartItem.getCreateDate() == null) {
            cartItem.setCreateDate(new Date());
        }
        if (cartItem.getModifyDate() == null) {
            cartItem.setModifyDate(new Date());
        }
        if (cartItem.getDeleteStatus() == null) {
            cartItem.setDeleteStatus(0);
        }
        omsCartItemMapper.insertSelective(cartItem);
    }

    @Override
    public void clear(Long memberId) {
        omsCartItemMapper.deleteByMemberId(memberId);
    }

    @Override
    public void delete(List<Long> ids) {
        omsCartItemMapper.deleteBatch(ids);
    }

    @Override
    public CartProduct getProduct(Long productId) {
        return pmsFeignClient.getCartProduct(productId).getData();
    }

    @Override
    public List<OmsCartItem> list(Long memberId) {
        return omsCartItemMapper.listByMemberId(memberId);
    }

    @Override
    public List<CartPromotionItem> listPromotion(Long memberId, List<Long> cartIds) {
        return omsCartItemMapper.listPromotion(memberId, cartIds);
    }

    @Override
    public void updateAttr(OmsCartItem cartItem) {
        if (cartItem.getModifyDate() == null) {
            cartItem.setModifyDate(new Date());
        }
        omsCartItemMapper.updateByPrimaryKeySelective(cartItem);
    }

    @Override
    public void updateQuantity(Long id, Integer quantity) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setId(id);
        cartItem.setQuantity(quantity);
        cartItem.setModifyDate(new Date());
        omsCartItemMapper.updateByPrimaryKeySelective(cartItem);
    }

    @Override
    public List<CartPromotionItem> listPromotionByUsername(String username, List<Long> cartIds) {
        UmsMember member = umsFeignClient.getMemberByUsername(username).getData();
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        return omsCartItemMapper.listPromotion(member.getId(), cartIds);
    }
}
