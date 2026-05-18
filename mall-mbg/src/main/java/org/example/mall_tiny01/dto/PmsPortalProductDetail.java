package org.example.mall_tiny01.dto;

import lombok.Data;
import org.example.mall_tiny01.mbg.model.*;

import java.io.Serializable;
import java.util.List;

@Data
public class PmsPortalProductDetail implements Serializable {
    
    private PmsProduct product;
    
    private PmsBrand brand;
    
    private List<SmsCoupon> couponList;
    
    private List<PmsProductAttribute> productAttributeList;
    
    private List<PmsProductAttributeValue> productAttributeValueList;
    
    private List<PmsProductFullReduction> productFullReductionList;
    
    private List<PmsProductLadder> productLadderList;
    
    private List<PmsSkuStock> skuStockList;
}
