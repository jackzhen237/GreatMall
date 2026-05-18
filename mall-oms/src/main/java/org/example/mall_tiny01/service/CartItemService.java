package org.example.mall_tiny01.service;

import org.example.mall_tiny01.dto.CartProduct;
import org.example.mall_tiny01.dto.CartPromotionItem;
import org.example.mall_tiny01.mbg.model.OmsCartItem;

import java.util.List;

public interface CartItemService {
    void add(OmsCartItem cartItem);

    void clear(Long memberId);

    void delete(List<Long> ids);

    CartProduct getProduct(Long productId);

    List<OmsCartItem> list(Long memberId);

    List<CartPromotionItem> listPromotion(Long memberId, List<Long> cartIds);

    void updateAttr(OmsCartItem cartItem);

    void updateQuantity(Long id, Integer quantity);

    List<CartPromotionItem> listPromotionByUsername(String username, List<Long> cartIds);
}
