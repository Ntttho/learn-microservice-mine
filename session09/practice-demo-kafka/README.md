# LESSON 05: THỰC CHIẾN CẤU HÌNH APACHE KAFKA TRONG MICROSERVICES

---

## I. Đặt vấn đề: Bài toán "Đặt hàng không chờ đợi" của StoreX

### 1.1. Bối cảnh
Bạn là thành viên trong đội phát triển hệ thống thương mại điện tử **StoreX**. Hệ thống đối mặt với tình trạng nghẽn cổ chai nghiêm trọng vào giờ cao điểm – khi có Flash Sale, hàng nghìn đơn hàng đổ về mỗi giây, kéo theo hàng loạt tác vụ phụ thuộc:
1. Trừ số lượng tồn kho (Inventory Service)
2. Xử lý thanh toán (Payment Service)
3. Gửi email & SMS thông báo (Notification Service)
4. Tích điểm khách hàng thân thiết (Reward Service)
5. Cập nhật dữ liệu thời gian thực (Analytics Service)

```
❌ Mô hình cũ (Đồng bộ - REST HTTP / Blocking):
Client ──> [ Order Service ] ──(chờ)──> [ Inventory ] ──(chờ)──> [ Payment ] ──(chờ)──> [ Notification ]
            ↳ Thời gian phản hồi tăng vọt (Latency cao), một service lỗi kéo theo toàn bộ sụp đổ (Cascading Failure).
```

### 1.2. Giải pháp: Kiến trúc hướng sự kiện (Event-Driven Architecture - EDA)
Chuyển sang kiến trúc phi đồng bộ với **Apache Kafka** làm xương sống truyền thông điệp (Event Broker):

```
✅ Mô hình mới (Bất đồng bộ - Event-Driven với Kafka):
Client ──> [ Order Service ] ──(Gửi event & phản hồi ngay)──> [ Kafka Topic: order-events ]
                                                                       │
             ┌─────────────────────────┬───────────────────────────────┴──────────────────────────────┐
             ▼                         ▼                               ▼                               ▼
     [ Inventory Service ]    [ Payment Service ]            [ Notification Service ]        [ Analytics Service ]
     (groupId: inventory)     (groupId: payment)             (groupId: notification)         (groupId: analytics)
```

---

## II. Các khái niệm cốt lõi cần nắm vững

| Khái niệm | Giải thích trực quan | Ý nghĩa / Mục đích |
| :--- | :--- | :--- |
| **Topic** | "Hộp thư" hoặc "Kênh truyền thông điệp" | Nơi lưu trữ các message có cùng chủ đề (ví dụ: `order-events`). |
| **Partition** | "Chia làn đường" trong 1 Topic | Giúp xử lý song song nhiều Consumer, tăng throughput. Đảm bảo thứ tự theo thời gian (FIFO) trong cùng 1 partition. |
| **Replication Factor** | "Bản sao lưu dự phòng" | Copy partition sang N Broker khác nhau. Chống mất dữ liệu khi có máy chủ bị sập (Fault Tolerance). |
| **Producer** | "Người gửi bưu phẩm" | Service tạo và đẩy tin nhắn vào Kafka (Dùng `KafkaTemplate`). |
| **Consumer** | "Người nhận bưu phẩm" | Service đọc và xử lý tin nhắn từ Kafka (Dùng `@KafkaListener`). |
| **Consumer Group (`groupId`)** | "Định danh nhóm nhận tin" | **Chỉ có ở phía Consumer**. Giúp chia tải giữa các instance trong cùng 1 service và lưu vết vị trí đọc (**Offset**). |
| **Serializer** | "Đóng gói bưu phẩm" | **Ở phía Producer**: Biến Java Object / String $\rightarrow$ mảng nhị phân `byte[]` để gửi qua mạng. |
| **Deserializer** | "Mở bưu phẩm" | **Ở phía Consumer**: Giải mã `byte[]` nhận từ Kafka $\rightarrow$ Java Object / String để ứng dụng xử lý. |

---

## III. Chuẩn bị môi trường: Cài đặt Kafka cục bộ (Local)

### 3.1. Chạy Kafka bằng Docker Compose (Khuyên dùng)
Tạo file `docker-compose.yml` tại thư mục gốc:

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

Khởi động cụm Kafka:
```bash
docker-compose up -d
```

### 3.2. Lệnh CLI hữu ích kiểm tra Topic
```bash
# Tạo topic với 3 partitions
docker exec -it kafka kafka-topics --create --topic order-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Xem danh sách topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Xem chi tiết topic (partitions, leader, replicas)
docker exec -it kafka kafka-topics --describe --topic order-events --bootstrap-server localhost:9092
```

---

## IV. Khai báo Dependency trong Spring Boot

### 4.1. Gradle (`build.gradle`)
```groovy
dependencies {
    // Spring Boot Starter Kafka
    implementation 'org.springframework.boot:spring-boot-starter-kafka'
    
    // Web Starter (REST API)
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
}
```

### 4.2. Maven (`pom.xml`)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## V. Cách 1: Cấu hình qua file `application.yml` (Nhanh & Tối ưu)

### 5.1. Phía Producer (`order-service`)
```yaml
server:
  port: 8080

spring:
  application:
    name: order-service
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      # Key luôn dùng StringSerializer
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      # Value dùng JsonSerializer để tự convert DTO Object sang JSON
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all               # Đảm bảo ghi an toàn vào mọi replica
      retries: 5              # Tự động retry khi mạng chập chờn
      properties:
        enable.idempotence: true # Tránh gửi trùng lặp message
```

### 5.2. Phía Consumer (`notification-service`)
```yaml
server:
  port: 8082

spring:
  application:
    name: notification-service
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service-group # Định danh duy nhất cho service này
      auto-offset-reset: earliest          # Đọc từ đầu nếu chưa có offset
      enable-auto-commit: true
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*" # BẮT BUỘC: Cho phép deserialize tất cả package
```

---

## VI. Định nghĩa Event DTO Model

Tạo một class POJO đại diện cho thông điệp sự kiện:

```java
package com.storex.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {
    private String eventId;
    private String orderId;
    private String customerEmail;
    private String productName;
    private Double price;
    private LocalDateTime createdAt;
}
```

---

## VII. Xây dựng Producer & REST Controller (`order-service`)

### 7.1. Service Producer gửi Event
```java
package com.storex.service;

import com.storex.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    public static final String TOPIC_ORDER_EVENTS = "order-events";

    // Inject KafkaTemplate (Spring Boot tự khởi tạo dựa vào YAML)
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderEvent event) {
        // Gửi với key = orderId để tất cả sự kiện của 1 đơn hàng vào cùng 1 Partition
        CompletableFuture<SendResult<String, OrderEvent>> future = 
                kafkaTemplate.send(TOPIC_ORDER_EVENTS, event.getOrderId(), event);

        // Xử lý callback bất đồng bộ để kiểm tra kết quả
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Gửi thành công event [{}] | Partition: {} | Offset: {}",
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ Gửi thất bại event [{}], lý do: {}", event.getOrderId(), ex.getMessage());
            }
        });
    }
}
```

### 7.2. Controller kích hoạt
```java
package com.storex.controller;

import com.storex.event.OrderEvent;
import com.storex.service.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderEventProducer orderEventProducer;

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestParam String productName,
                                              @RequestParam String email,
                                              @RequestParam Double price) {
        String orderId = UUID.randomUUID().toString();

        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .customerEmail(email)
                .productName(productName)
                .price(price)
                .createdAt(LocalDateTime.now())
                .build();

        // Bắn sự kiện lên Kafka (Non-blocking)
        orderEventProducer.sendOrderCreatedEvent(event);

        // Trả kết quả ngay lập tức cho client
        return ResponseEntity.ok("Đơn hàng [" + orderId + "] đã được tiếp nhận thành công!");
    }
}
```

---

## VIII. Xây dựng Consumer nhận Event (`notification-service`)

### 8.1. Consumer lắng nghe sự kiện
```java
package com.storex.consumer;

import com.storex.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderNotificationConsumer {

    @KafkaListener(
            topics = "order-events",
            groupId = "notification-service-group"
    )
    public void handleOrderNotification(OrderEvent orderEvent) {
        log.info("📧 [Notification Service] Nhận được sự kiện đơn hàng: {}", orderEvent.getOrderId());
        log.info("-> Gửi email xác nhận đến: {}", orderEvent.getCustomerEmail());
        log.info("-> Tên sản phẩm: {} | Tổng tiền: {}", orderEvent.getProductName(), orderEvent.getPrice());
        // Xử lý gửi email hoặc thông báo...
    }
}
```

---

## IX. Cách 2: Cấu hình nâng cao bằng Java `@Configuration`

Nếu bạn muốn tùy biến sâu (như tạo Retry, Dead Letter Queue, Filter Message), có thể dùng class Java Config thay cho `application.yml`:

### 9.1. Producer Config (`KafkaProducerConfig.java`)
```java
package com.storex.config;

import com.storex.event.OrderEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, OrderEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        // ⚠️ Chú ý: Không có 'http://'
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### 9.2. Consumer Config (`KafkaConsumerConfig.java`)
```java
package com.storex.config;

import com.storex.event.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // Cho phép deserialize DTO
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

---

## X. Cấu hình Error Handling & Dead Letter Queue (DLQ)

Khi Consumer gặp lỗi không thể xử lý (ví dụ database sập, dữ liệu sai định dạng), ta cấu hình **Retry 3 lần** và nếu vẫn lỗi thì đẩy sang **DLQ Topic** (`order-events.DLT`) để điều tra sau:

```java
@Configuration
public class KafkaDLQConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> kafkaListenerContainerFactoryWithDLQ(
            ConsumerFactory<String, OrderEvent> consumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Retry 3 lần, mỗi lần cách nhau 1 giây -> sau đó gửi vào DLQ
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(1000L, 3L)
        );

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
```

---

## XI. Quy tắc "Bất di bất dịch" khi cấu hình Kafka

1. **`groupId` là duy nhất cho mỗi Microservice**:
   * Mỗi service nghiệp vụ phải có 1 `groupId` riêng biệt (ví dụ: `notification-group`, `inventory-group`, `payment-group`).
   * ❌ Không bao giờ dùng chung 1 `groupId` cho 2 microservice khác nhau, vì chúng sẽ tranh giành và chia nhau message thay vì cùng nhận đủ message.
2. **Cặp đôi Serializer $\leftrightarrow$ Deserializer phải tương thích**:
   * Gửi `StringSerializer` $\rightarrow$ Nhận `StringDeserializer`.
   * Gửi `JsonSerializer` $\rightarrow$ Nhận `JsonDeserializer`.
3. **Luôn quản lý Partition phù hợp với số lượng Consumer**:
   * Nếu Topic có 3 Partitions $\rightarrow$ Tối đa 3 Consumer trong cùng 1 group có thể đọc đồng thời. Nếu bạn bật 5 Consumer, thì 2 Consumer sẽ bị nhàn rỗi (idle).

---

## XII. Những "Cú lừa" & Lỗi kinh điển trong thực tế (Kèm cách Fix)

### 🔴 Cú lừa 1: Ghi `http://` vào `bootstrap-servers`
* **Lỗi**: `bootstrap-servers: "http://localhost:9092"`
* **Nguyên nhân**: Kafka dùng giao thức nhị phân TCP, không phải HTTP.
* **Fix**: Sửa thành `"localhost:9092"` hoặc `"127.0.0.1:9092"`.

---

### 🔴 Cú lừa 2: `NullPointerException: Cannot invoke ... kafkaTemplate.send(...)`
* **Lỗi**: Gọi `kafkaTemplate.send()` bị `NullPointerException`.
* **Nguyên nhân**: Dùng `@RequiredArgsConstructor` nhưng quên từ khóa `final` ở biến `KafkaTemplate`:
  ```java
  private KafkaTemplate<String, Object> kafkaTemplate; // ❌ Thiếu 'final' -> Lombok không tạo constructor
  ```
* **Fix**: Thêm `final` để Lombok và Spring tự động inject Bean:
  ```java
  private final KafkaTemplate<String, Object> kafkaTemplate; // ✅
  ```

---

### 🔴 Cú lừa 3: Import nhầm Serializer của Jackson
* **Lỗi**: `import tools.jackson.databind.ser.jdk.StringSerializer;`
* **Nguyên nhân**: IDE tự động gợi ý class nội bộ của Jackson thay vì của Kafka.
* **Fix**: Đảm bảo import đúng gói Kafka:
  ```java
  import org.apache.kafka.common.serialization.StringSerializer; // ✅
  ```

---

### 🔴 Cú lừa 4: Quên cấu hình `spring.json.trusted.packages`
* **Lỗi**: `IllegalArgumentException: The class '...' is not in the trusted packages`
* **Nguyên nhân**: `JsonDeserializer` mặc định chặn deserialize để bảo mật chống lỗ hổng Injection.
* **Fix**: Thêm thuộc tính cấu hình cho phép:
  ```yaml
  spring.kafka.consumer.properties.spring.json.trusted.packages: "*"
  ```

---

### 🔴 Cú lừa 5: Trùng `server.port: 8080` giữa các service
* **Lỗi**: `Port 8080 was already in use.` khi chạy đồng thời Producer và Consumer.
* **Fix**: Khai báo port riêng trong `application.yaml` (ví dụ: `order-service: 8080`, `notification-service: 8082`).
