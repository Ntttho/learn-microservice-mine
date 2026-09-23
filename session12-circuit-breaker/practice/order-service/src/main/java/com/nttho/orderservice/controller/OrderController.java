package com.nttho.orderservice.controller;

import com.nttho.orderservice.client.ProductClient;
import com.nttho.orderservice.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final ProductClient productClient;

    @GetMapping("/products")
    public List<ProductDto> orderAll(){
        return productClient.findAll();
    }

    @GetMapping("/products/{id}")
    public ProductDto orderProduct(@PathVariable String id){
        return productClient.findById(id);
    }
}
