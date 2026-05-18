package org.example.mall_tiny01.controller;

import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.component.UserContext;
import org.example.mall_tiny01.dto.CartProduct;
import org.example.mall_tiny01.dto.CartPromotionItem;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.OmsCartItem;
import org.example.mall_tiny01.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartItemController {
    @Autowired
    private CartItemService cartItemService;

    @PostMapping("/add")
    @ApiOperation("添加商品到购物车")
    public Result add(@RequestBody OmsCartItem cartItem) {
        cartItemService.add(cartItem);
        return Result.success();
    }

    @PostMapping("/clear")
    @ApiOperation("清空当前会员的购物车")
    public Result clear(@RequestParam Long memberId) {
        cartItemService.clear(memberId);
        return Result.success();
    }

    @PostMapping("/delete")
    @ApiOperation("删除购物车中的指定商品")
    public Result delete(@RequestParam List<Long> ids) {
        cartItemService.delete(ids);
        return Result.success();
    }

    @GetMapping("/getProduct/{productId}")
    @ApiOperation("获取购物车中指定商品的规格,用于重选规格")
    public Result<CartProduct> getProduct(@PathVariable Long productId) {
        CartProduct cartProduct = cartItemService.getProduct(productId);
        return Result.success(cartProduct);
    }

    @GetMapping("/list")
    @ApiOperation("获取当前会员的购物车列表")
    public Result<List<OmsCartItem>> list(@RequestParam Long memberId) {
        List<OmsCartItem> list = cartItemService.list(memberId);
        return Result.success(list);
    }

    @GetMapping("/list/promotion")
    @ApiOperation("获取当前会员的购物车列表,包括促销信息")
    public Result<List<CartPromotionItem>> listPromotion(
            @RequestParam(value = "cartIds", required = false) List<Long> cartIds) {
        String username = UserContext.getUsername();
        List<CartPromotionItem> list = cartItemService.listPromotionByUsername(username, cartIds);
        return Result.success(list);
    }

    @PostMapping("/update/attr")
    @ApiOperation("修改购物车中商品的规格")
    public Result updateAttr(@RequestBody OmsCartItem cartItem) {
        cartItemService.updateAttr(cartItem);
        return Result.success();
    }

    @GetMapping("/update/quantity")
    @ApiOperation("修改购物车中指定商品的数量")
    public Result updateQuantity(@RequestParam Long id, @RequestParam Integer quantity) {
        cartItemService.updateQuantity(id, quantity);
        return Result.success();
    }
}