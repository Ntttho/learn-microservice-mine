package com.nttho.orderservice.client;

import com.nttho.orderservice.model.dto.response.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient{
    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable Long id);
}
