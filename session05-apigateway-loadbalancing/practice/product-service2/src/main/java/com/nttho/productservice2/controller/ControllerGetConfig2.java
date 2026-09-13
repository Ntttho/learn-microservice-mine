package com.nttho.productservice2.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-service")
public class ControllerGetConfig2 {
    @Value("${app.variable}")
    private String variable;

    @GetMapping()
    public String getVariable(){
        return variable + " 123";
    }
}
