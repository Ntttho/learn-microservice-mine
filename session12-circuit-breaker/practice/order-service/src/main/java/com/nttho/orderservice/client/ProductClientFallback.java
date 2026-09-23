package com.nttho.orderservice.client;

import com.nttho.orderservice.dto.ProductDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public List<ProductDto> findAll() {
        return Collections.emptyList();
    }

    @Override
    public ProductDto findById(String id) {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName("Product Fallback (Product-service unavailable / not found)");
        dto.setPrice(0.0);
        dto.setAmount(0);
        return dto;
    }

    @Override
    public ProductDto createProduct() {
        return null;
    }
}
