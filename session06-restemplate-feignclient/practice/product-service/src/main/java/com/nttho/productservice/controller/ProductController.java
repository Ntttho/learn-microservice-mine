package com.nttho.productservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    long[] productIdList = {1, 2, 3, 4};
    @GetMapping("/{id}/enable")
    public Boolean productIsEnable(@PathVariable long id){
        return Arrays.stream(productIdList).filter(i -> i == id ).findAny().isPresent();
    }
}
