package com.nttho.productservice.controller;

import com.nttho.productservice.dto.request.ProductRequest;
import com.nttho.productservice.dto.request.ProductStockCommand;
import com.nttho.productservice.entity.Product;
import com.nttho.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
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

    @PostMapping("/save")
    public Product saveProduct(@RequestBody ProductRequest productRequest){
        return productService.saveProduct(productRequest);
    }

    @PutMapping("/reduce-stock")
    public Product reduceStockProduct(@RequestBody ProductStockCommand request){
        return productService.reduceProductStock(request);
    }

    @PutMapping("/restore-stock")
    public void compensatingProduct(@RequestBody ProductStockCommand request){
        productService.compensatingProduct(request);
    }
}
