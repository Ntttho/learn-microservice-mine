package com.nttho.productservice.controller;

import com.nttho.productservice.common.command.ProductCommand;
import com.nttho.productservice.common.dto.request.ProductRequest;
import com.nttho.productservice.entity.Product;
import com.nttho.productservice.service.ProductService;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    @GetMapping
    public List<Product> findAll(){
        return productService.findAll();
    }
    @GetMapping("/{id}")
    public Product findById(@PathVariable Long id){
        return productService.findById(id);
    }

    @PutMapping("/reduce-stock")
    public void reduceStock(@RequestBody ProductCommand command){
        productService.reduceStock(command);
    }

    @PutMapping("/compensation")
    public void productCompensation(@RequestBody ProductCommand command){
        productService.compensation(command);
    }

    @PostMapping("/create")
    public Product createProduct(@RequestBody ProductRequest request){
        return productService.createProduct(request);
    }

}
