package org.example.mall_tiny01.dto;

import lombok.Data;
import org.example.mall_tiny01.mbg.model.CmsSubject;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.mbg.model.SmsHomeAdvertise;

import java.util.List;

@Data
public class HomeContentResult {
    private List<SmsHomeAdvertise> advertiseList;
    private List<PmsBrand> brandList;
    private HomeFlashPromotion homeFlashPromotion;
    private List<PmsProduct> hotProductList;
    private List<PmsProduct> newProductList;
    private List<CmsSubject> subjectList;
}