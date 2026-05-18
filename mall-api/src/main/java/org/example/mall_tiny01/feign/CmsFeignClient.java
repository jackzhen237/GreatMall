package org.example.mall_tiny01.feign;

import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.CmsSubject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "mall-cms")
public interface CmsFeignClient {

    @GetMapping("/subject/listAll")
    Result<List<CmsSubject>> listAllSubjects();
}
