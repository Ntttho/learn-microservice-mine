package com.nttho.productservice1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication

public class ProductService1Application {

    public static void main(String[] args) {
        SpringApplication.run(ProductService1Application.class, args);
    }

}
