package com.nttho.productservice.controller;

import com.nttho.productservice.ProductServiceApplication;
import com.nttho.productservice.entity.Product;
import com.nttho.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    @GetMapping
    public List<Product> findAll(){
        return ProductRepository.listProduct;
    }

    @GetMapping("/{id}")
    public Product findById(@PathVariable String id){
        return ProductRepository.listProduct.stream().filter(i -> i.getId().equals(id))
                .findAny()
                .orElseThrow(() -> new RuntimeException("product-service findById failed"))
                ;
    }

    @PostMapping()
    public Product createProduct(){
        Product product = new Product();
        product.setName("Product " + ProductRepository.listProduct.size() + 1);
        product.setPrice(1000.00);
        product.setAmount(1000);
        ProductRepository.listProduct.add(product);

        return product;
    }
}
