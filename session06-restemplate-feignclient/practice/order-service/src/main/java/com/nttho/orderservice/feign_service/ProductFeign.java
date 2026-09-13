package com.nttho.orderservice.feign_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeign {
    @GetMapping("/api/product/{id}/enable")
    public Boolean enableProduct(@PathVariable(required = true) Long id);
}

