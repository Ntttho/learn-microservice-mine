# Hướng Dẫn Toàn Tập: Circuit Breaker & Resilience4j Trong Spring Cloud

Tài liệu này tóm tắt toàn bộ kiến thức, các lỗi thường gặp, hướng dẫn cấu hình và 2 cách triển khai **Fallback với OpenFeign & Resilience4j** trong kiến trúc Microservices.

---

## 1. Cơ Chế Hoạt Động Của Circuit Breaker (State Machine)

Circuit Breaker hoạt động tương tự như một **cầu dao điện thông minh tự động ngắt** khi phát hiện sự cố và **tự động phục hồi** (Self-healing):

```text
                 Tỷ lệ lỗi >= 50%
  ┌──────────┐ ────────────────────> ┌──────────┐
  │  CLOSED  │                       │   OPEN   │
  │ (Bình    │                       │ (Ngắt    │
  │  thường) │ <───────────────────  │  mạch)   │
  └──────────┘    Thử nghiệm         └──────────┘
       ▲          thành công               │
       │                                   │ Hết thời gian chờ (10s)
       │                                   ▼
       └───────────────────────────── ┌───────────┐
            Nếu thử nghiệm thất bại   │ HALF_OPEN │
            (Quay lại trạng thái OPEN)│ (Thử      │
                                      │  nghiệm)  │
                                      └───────────┘
```

### Các trạng thái:
1. 🟢 **CLOSED (Đóng mạch - Hoạt động bình thường):**
   - Mọi request từ `order-service` được chuyển thẳng sang `product-service`.
   - Resilience4j liên tục ghi nhận kết quả các cuộc gọi trong cửa sổ trượt (sliding window).
2. 🔴 **OPEN (Mở mạch - Ngắt kết nối):**
   - Khi tỷ lệ lỗi vượt ngưỡng cho phép (ví dụ: $\ge 50\%$).
   - Circuit Breaker **ngắt hoàn toàn**, không gửi request qua mạng nữa mà lập tức chuyển sang hàm **Fallback** để giảm tải và tránh làm sập dây chuyền (Cascading Failure).
3. 🟡 **HALF_OPEN (Nửa mở - Thử nghiệm phục hồi):**
   - Sau thời gian chờ ở trạng thái OPEN (ví dụ: 10 giây), hệ thống tự động chuyển sang HALF_OPEN.
   - Cho phép một lượng nhỏ request thử nghiệm (ví dụ: 3 request) đi qua:
     - **Thành công:** Tự động khôi phục về **CLOSED**.
     - **Thất bại:** Quay lại trạng thái **OPEN** và tiếp tục chờ.

---

## 2. Giám Sát Trạng Thái Bằng Spring Boot Actuator

> [!NOTE]
> - **Resilience4j:** Đóng vai trò là bộ máy tự động ngắt và tự động phục hồi mạch.
> - **Actuator:** Đóng vai trò là màn hình hiển thị/API giám sát cho lập trình viên và hệ thống DevOps.

### Các Endpoints Actuator quan trọng:

| Endpoint | Phương thức | Mục đích |
| :--- | :---: | :--- |
| `/actuator/health` | `GET` | Xem tổng quan trạng thái service và trạng thái cụ thể của Circuit Breaker (`CLOSED`, `OPEN`, `HALF_OPEN`). |
| `/actuator/circuitbreakers` | `GET` | Xem danh sách toàn bộ các instance Circuit Breaker đang chạy. |
| `/actuator/circuitbreakerevents` | `GET` | Xem lịch sử các sự kiện chuyển mạch, lỗi, request thành công/thất bại. |
| `/actuator/circuitbreakerevents/{name}` | `GET` | Xem chi tiết sự kiện của riêng 1 instance (ví dụ: `productService`). |

---

## 3. Giải Thích Cấu Hình File `application.yaml`

Dưới đây là cấu hình hoàn chỉnh và giải thích chi tiết từng thuộc tính:

```yaml
server:
  port: 8082

spring:
  application:
    name: order-service

  # Cấu hình Spring AOP
  aop:
    auto: true                  # Bật tự động cấu hình Spring AOP (mặc định: true)
    proxy-target-class: true    # Sử dụng CGLIB proxy cho class

  # Cấu hình OpenFeign CircuitBreaker
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true           # BẬT true nếu dùng Fallback Class trực tiếp trên @FeignClient
                                # ĐẶT false nếu muốn dùng annotation @CircuitBreaker tại Controller/Service

eureka:
  client:
    fetch-registry: true
    register-with-eureka: true
    service-url:
      defaultZone: http://localhost:8761/eureka
  instance:
    prefer-ip-address: true

# Cấu hình tham số Resilience4j Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      product-service:                             # Tên instance quản lý
        sliding-window-type: COUNT_BASED           # Loại cửa sổ: COUNT_BASED (số lượng) hoặc TIME_BASED (thời gian)
        sliding-window-size: 10                    # Kích thước cửa sổ: 10 request gần nhất
        minimum-number-of-calls: 5                 # Số lượng cuộc gọi tối thiểu trước khi bắt đầu tính tỷ lệ lỗi
        failure-rate-threshold: 50                 # Ngưỡng lỗi (%): >= 50% lỗi sẽ chuyển sang OPEN
        wait-duration-in-open-state: 10000ms       # Thời gian chờ ở trạng thái OPEN trước khi sang HALF_OPEN (10s)
        permitted-number-of-calls-in-half-open-state: 3 # Số request thử nghiệm được phép đi qua khi ở HALF_OPEN
        automatic-transition-from-open-to-half-open-enabled: true # Tự động chuyển OPEN -> HALF_OPEN khi hết thời gian chờ
        register-health-indicator: true            # Đăng ký thông tin trạng thái vào /actuator/health

# Cấu hình mở các Endpoint Actuator
management:
  endpoints:
    web:
      exposure:
        include: "health,info,circuitbreakers,circuitbreakerevents"
  endpoint:
    health:
      show-details: always                         # Luôn hiển thị chi tiết chi tiết component health
  health:
    circuitbreakers:
      enabled: true                                # Bật health check cho circuit breaker
```

---

## 4. Hướng Dẫn 2 Cách Triển Khai Fallback Cho Feign Client

### CÁCH 1: Dùng Fallback Class trên `@FeignClient` (Khuyên Dùng Nhất)
Đây là cách tiêu chuẩn và tối ưu nhất khi sử dụng Spring Cloud OpenFeign.

#### Bước 1: Bật Feign Circuit Breaker trong `application.yaml`
```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
```

#### Bước 2: Tạo Fallback Class implement Feign Interface
```java
package com.nttho.orderservice.client;

import com.nttho.orderservice.dto.ProductDto;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public List<ProductDto> findAll() {
        return Collections.emptyList();
    }

    @Override
    public ProductDto findById(String id) {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName("Fallback Product (Service Down / Not Found)");
        dto.setPrice(0.0);
        dto.setAmount(0);
        return dto;
    }

    @Override
    public ProductDto createProduct() {
        return null;
    }
}
```

#### Bước 3: Gắn `fallback` vào `@FeignClient`
```java
package com.nttho.orderservice.client;

import com.nttho.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

@FeignClient(name = "product-service", path = "/api/products", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping
    List<ProductDto> findAll();

    @GetMapping("/{id}")
    ProductDto findById(@PathVariable String id);

    @PostMapping
    ProductDto createProduct();
}
```

#### Bước 4: Controller gọi bình thường (Code cực kỳ gọn)
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final ProductClient productClient;

    @GetMapping("/products/{id}")
    public ProductDto orderProduct(@PathVariable String id) {
        return productClient.findById(id); // Tự động fallback nếu có lỗi
    }
}
```

---

### CÁCH 2: Dùng Annotation `@CircuitBreaker` của Resilience4j

Cách này dùng khi bạn muốn gắn Circuit Breaker bằng Annotation lên tầng Service hoặc method tùy chỉnh.

#### Bước 1: Tắt Circuit Breaker nội bộ của Feign trong `application.yaml`
*(Để Feign ném lỗi mạng/500 lên cho `@CircuitBreaker` bắt lấy)*
```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: false
```

#### Bước 2: Viết Method và Fallback Method tuân thủ đúng 3 nguyên tắc

> [!IMPORTANT]
> **Quy tắc bắt buộc khi viết Fallback Method:**
> 1. **Phạm vi truy cập:** Phải là `public` (để AOP Reflection có thể gọi).
> 2. **Kiểu trả về:** Phải giống hệt kiểu trả về của method chính (`ProductDto`).
> 3. **Tham số:** Phải nhận đủ các tham số của method chính + thêm tham số `Throwable` (hoặc `Exception`) ở cuối cùng: `(String id, Throwable throwable)`.

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final ProductClient productClient;

    @GetMapping("/products/{id}")
    @CircuitBreaker(name = "product-service", fallbackMethod = "findByIdFallBack")
    public ProductDto orderProduct(@PathVariable String id) {
        return productClient.findById(id);
    }

    // Method Fallback
    public ProductDto findByIdFallBack(String id, Throwable throwable) {
        System.out.println("Circuit Breaker Fallback kích hoạt, lý do: " + throwable.getMessage());
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName("Fallback Product");
        dto.setPrice(0.0);
        dto.setAmount(0);
        return dto;
    }
}
```

---

## 5. Các Lỗi Phổ Biến Cần Tránh

1. **Lỗi `NoFallbackAvailableException: No fallback available`:**
   - **Nguyên nhân:** Đang bật `spring.cloud.openfeign.circuitbreaker.enabled: true` nhưng interface `@FeignClient` lại không khai báo thuộc tính `fallback = ...`.
2. **Lỗi `NoSuchMethodException` / Không nhảy vào Fallback:**
   - **Nguyên nhân:** Viết sai chữ ký hàm Fallback (sai tham số, sai kiểu trả về, hoặc để `private`).
3. **Lỗi `Could not find org.springframework.boot:spring-boot-starter-aop`:**
   - **Nguyên nhân:** Khai báo thừa starter AOP không tương thích phiên bản; trong khi `spring-cloud-starter-circuitbreaker-resilience4j` đã tích hợp sẵn.
