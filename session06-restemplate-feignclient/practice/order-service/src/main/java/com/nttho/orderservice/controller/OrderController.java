package com.nttho.orderservice.controller;

import com.nttho.orderservice.feign_service.ProductFeign;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {
    protected final ProductFeign productFeign;

    @Value("${app.variable}")
    private String variable;

    @GetMapping
    public String getVariableTest(){
        return variable;
    }

    @GetMapping("/product/{id}")
    public String orderProduct(@PathVariable Long id){
        if (productFeign.enableProduct(id)){
            return UUID.randomUUID().toString();
        }
        return "Order product failed";
    }
}
