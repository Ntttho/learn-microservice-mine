package com.nttho.productservice.service;

import com.nttho.productservice.common.command.ProductCommand;
import com.nttho.productservice.common.dto.request.ProductRequest;
import com.nttho.productservice.entity.Product;
import com.nttho.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id")
    public Product findById(Long id){
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> findAll(){
        return productRepository.findAll();
    }

    @Transactional
    @CachePut(value = "products", key = "#command.productId")
    public Product reduceStock(ProductCommand command){
        Product product = productRepository.findById(command.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getQuantity() < command.getQuantity()){
            throw new RuntimeException("not enough quantity of product");
        }

        product.setQuantity(product.getQuantity() - command.getQuantity());
        return productRepository.save(product);
    }

    @Transactional
    @CachePut(value = "products", key = "#command.productId")
    public Product compensation(ProductCommand command){
        Product product = productRepository.findById(command.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));
//        if (product.getQuantity() < command.getQuantity()){
//            throw new RuntimeException("not enough quantity of product");
//        }

        product.setQuantity(product.getQuantity() + command.getQuantity());
        return productRepository.save(product);
    }

    public Product createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        return productRepository.save(product);
    }
}
