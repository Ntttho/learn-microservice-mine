package com.nttho.productservice.service;

import com.nttho.productservice.dto.request.ProductRequest;
import com.nttho.productservice.dto.request.ProductStockCommand;
import com.nttho.productservice.entity.Product;
import com.nttho.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    // findall, findbyid, save, reducestock
    public List<Product> findAll(){
        return productRepository.findAll();
    }

    public Product findById(Long id){
        return productRepository.findById(id).orElseThrow();
    }

    public Product saveProduct(ProductRequest request){
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        return productRepository.save(product);
    }

    public Product reduceProductStock(ProductStockCommand command){
        Product product = findById(command.getId());

        if (product == null){
            throw new RuntimeException("Product not found");
        }
        if (product.getStock() < command.getStock()){
            throw new IllegalArgumentException("Insufficient stock of product: "  +command.getId());
        }

        product.setStock(product.getStock() - command.getStock());
        productRepository.save(product);
        return product;
    }

    public void compensatingProduct(ProductStockCommand command){
        Product product = findById(command.getId());
        product.setStock(product.getStock() + command.getStock());
        productRepository.save(product);
//        return product;
    }
}
