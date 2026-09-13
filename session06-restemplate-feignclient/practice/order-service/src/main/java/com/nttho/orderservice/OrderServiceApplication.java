package com.nttho.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
/*
    // b1 thêm vao build gradle openfeign
    // b2 @EnableFeignClient ở application chính
    // b3 tạo feign client interface (đăng ký cho service mà chính nó muốn giao tiếp)
    // b4 Inject và sử dụng trong Service / Controller

    // Lợi ích của OpenFeign:
    1. Không cần hardcode IP/Port: Tự động tra cứu product-service từ Eureka Server.
    2. Tự động Load Balancing: Tự động chia tải nếu product-service có nhiều instance.
    3. Code cực kỳ ngắn gọn: Không cần viết code HTTP Client phức tạp như RestTemplate hay WebClient.
 */