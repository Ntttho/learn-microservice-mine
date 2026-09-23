package com.nttho.orderservice.client;

import com.nttho.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;



@FeignClient(name = "product-service", path = "/api/products", fallback = ProductClientFallback.class)
public interface ProductClient {
    @GetMapping
    public List<ProductDto> findAll();

    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable String id);

    @PostMapping()
    public ProductDto createProduct();
}
