package com.nttho.productservice;

import com.nttho.productservice.entity.Product;
import com.nttho.productservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                List<Product> sampleProducts = List.of(
                        Product.builder().name("iPhone 15 Pro Max").stock(10).price(1200.0).build(),
                        Product.builder().name("Samsung Galaxy S24 Ultra").stock(15).price(1100.0).build(),
                        Product.builder().name("MacBook Pro M3").stock(5).price(2000.0).build(),
                        Product.builder().name("AirPods Pro 2").stock(20).price(250.0).build(),
                        Product.builder().name("Chuột Logitech MX Master 3S").stock(2).price(100.0).build()
                );
                productRepository.saveAll(sampleProducts);
                System.out.println(">>> Đã khởi tạo " + sampleProducts.size() + " sản phẩm mẫu thành công!");
            }
        };
    }

}
