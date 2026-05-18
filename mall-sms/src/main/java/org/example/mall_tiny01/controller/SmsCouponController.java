package org.example.mall_tiny01.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.dto.SmsCouponParamDTO;
import org.example.mall_tiny01.mbg.mapper.SmsCouponHistoryMapper;
import org.example.mall_tiny01.mbg.mapper.SmsCouponMapper;
import org.example.mall_tiny01.mbg.mapper.SmsCouponProductCategoryRelationMapper;
import org.example.mall_tiny01.mbg.mapper.SmsCouponProductRelationMapper;
import org.example.mall_tiny01.mbg.model.SmsCoupon;
import org.example.mall_tiny01.mbg.model.SmsCouponHistory;
import org.example.mall_tiny01.mbg.model.SmsCouponProductCategoryRelation;
import org.example.mall_tiny01.mbg.model.SmsCouponProductRelation;
import org.example.mall_tiny01.service.SmsCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon")
@Api(tags = "优惠券管理")
public class SmsCouponController {
    @Autowired
    private SmsCouponService smsCouponService;

    @Autowired
    private SmsCouponMapper couponMapper;

    @Autowired
    private SmsCouponProductRelationMapper productRelationMapper;

    @Autowired
    private SmsCouponProductCategoryRelationMapper categoryRelationMapper;

    @Autowired
    private SmsCouponHistoryMapper couponHistoryMapper;

    @PostMapping("/create")
    @ApiOperation("添加优惠券")
    public Result add(@RequestBody SmsCouponParamDTO couponParam){
        smsCouponService.save(couponParam);
        return Result.success("添加成功");
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除优惠券")
    public Result delete(@PathVariable Long id){
        smsCouponService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("根据优惠券名称和类型分页获取优惠券列表")
    public Result list(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        
        PageResult<SmsCoupon> result = smsCouponService.list(name, type, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/update/{id}")
    @ApiOperation("根据ID修改优惠券")
    public Result update(@PathVariable Long id, @RequestBody SmsCouponParamDTO couponParam){
        smsCouponService.update(id, couponParam);
        return Result.success("修改成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("获取单个优惠券的详细信息")
    public Result<SmsCouponParamDTO> getById(@PathVariable Long id){
        SmsCouponParamDTO result = smsCouponService.getById(id);
        return Result.success(result);
    }

    @GetMapping("/productRelation/{couponId}")
    @ApiOperation("获取优惠券关联商品列表")
    public Result<List<SmsCouponProductRelation>> listProductRelations(@PathVariable Long couponId) {
        List<SmsCouponProductRelation> list = productRelationMapper.selectByCouponId(couponId);
        return Result.success(list);
    }

    @GetMapping("/categoryRelation/{couponId}")
    @ApiOperation("获取优惠券关联分类列表")
    public Result<List<SmsCouponProductCategoryRelation>> listCategoryRelations(@PathVariable Long couponId) {
        List<SmsCouponProductCategoryRelation> list = categoryRelationMapper.selectByCouponId(couponId);
        return Result.success(list);
    }

    @GetMapping("/listAll")
    @ApiOperation("根据类型获取所有优惠券（不分页）")
    public Result<List<SmsCoupon>> listAll(@RequestParam(value = "name", required = false) String name,
                                            @RequestParam(value = "type", required = false) Integer type) {
        List<SmsCoupon> result = smsCouponService.listAll(name, type);
        return Result.success(result);
    }

    @GetMapping("/history/list")
    @ApiOperation("获取优惠券使用历史")
    public Result<List<SmsCouponHistory>> listHistory(@RequestParam(value = "memberId", required = false) Long memberId,
                                                       @RequestParam(value = "useStatus", required = false) Integer useStatus) {
        List<SmsCouponHistory> list = couponHistoryMapper.list(memberId, useStatus, null);
        return Result.success(list);
    }

    @GetMapping("/simple/{id}")
    @ApiOperation("获取优惠券基本信息（供远程调用）")
    public Result<SmsCoupon> getCouponSimple(@PathVariable Long id) {
        SmsCoupon coupon = couponMapper.selectByPrimaryKey(id);
        return Result.success(coupon);
    }

    @PostMapping("/history/add")
    @ApiOperation("添加优惠券领取记录（供远程调用）")
    public Result addHistory(@RequestBody SmsCouponHistory history) {
        couponHistoryMapper.insertSelective(history);
        return Result.success("添加成功");
    }
}
