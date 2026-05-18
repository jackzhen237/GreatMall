package org.example.mall_tiny01.feign;

import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.UmsIntegrationConsumeSetting;
import org.example.mall_tiny01.mbg.model.UmsMember;
import org.example.mall_tiny01.mbg.model.UmsMemberReceiveAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mall-ums")
public interface UmsFeignClient {

    @GetMapping("/sso/info")
    Result<UmsMember> getMemberByUsername(@RequestParam("username") String username);

    @GetMapping("/sso/member/{id}")
    Result<UmsMember> getMemberById(@PathVariable("id") Long id);

    @GetMapping("/member/address/list")
    Result<List<UmsMemberReceiveAddress>> listAddress(@RequestParam("memberId") Long memberId);

    @GetMapping("/member/address/{id}")
    Result<UmsMemberReceiveAddress> getAddressById(@PathVariable("id") Long id);

    @GetMapping("/integrationConsumeSetting/{id}")
    Result<UmsIntegrationConsumeSetting> getIntegrationConsumeSetting(@PathVariable("id") Long id);
}
