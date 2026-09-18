# Session 09: Cấu hình Apache Kafka & Spring Boot Order Service

Dự án này hướng dẫn cấu hình **Apache Kafka (KRaft mode)** với **Docker Compose**, **Kafka UI** và xây dựng ứng dụng **Order Service** kết nối Kafka (vừa là Producer vừa là Consumer).

---

## 1. Cấu trúc thư mục

```text
session09/
├── docker-compose.yaml        # Khởi chạy Kafka (KRaft) & Kafka UI
├── README.md                  # Hướng dẫn chi tiết
└── order-service/             # Dự án Spring Boot
    ├── build.gradle           # Cấu hình Spring Boot & spring-kafka
    ├── src/main/resources/
    │   └── application.yaml   # Cấu hình kết nối Kafka Producer & Consumer
    └── src/main/java/com/nttho/orderservice/
        ├── OrderServiceApplication.java
        ├── config/
        │   └── KafkaTopicConfig.java     # Cấu hình Topic tự động khởi tạo
        ├── controller/
        │   └── OrderController.java      # REST API tạo đơn hàng
        ├── dto/
        │   ├── OrderRequest.java         # DTO đầu vào API
        │   └── OrderEvent.java           # DTO gửi lên Kafka Topic
        └── service/
            ├── OrderProducerService.java # Gửi message lên Kafka
            └── OrderConsumerService.java # Nhận (@KafkaListener) và xử lý message
```

---

## 2. Hướng dẫn chạy Kafka bằng Docker

Di chuyển vào thư mục `session09` và chạy lệnh:

```bash
docker compose up -d
```

- **Kafka Broker**: `localhost:9092` (PLAINTEXT)
- **Kafka UI**: Truy cập giao diện quản trị tại [http://localhost:8089](http://localhost:8089)

Để kiểm tra trạng thái container:
```bash
docker compose ps
```

---

## 3. Khởi chạy Order Service

Di chuyển vào thư mục `session09/order-service` và chạy:

```bash
.\gradlew.bat bootRun
```

Server sẽ khởi động tại cổng **`8081`** và tự động kết nối đến Kafka tại `localhost:9092`. Topic `order-events` (3 partitions, replication factor 1) sẽ được tự động tạo.

---

## 4. Kiểm tra hoạt động (Testing)

### 4.1. Tạo đơn hàng (Producer gửi message lên Kafka)

Gửi request HTTP `POST` tới API:

**Endpoint**: `http://localhost:8081/api/orders`  
**Method**: `POST`  
**Header**: `Content-Type: application/json`  
**Body**:
```json
{
  "userId": "user-001",
  "productCode": "PROD-MACBOOK-M3",
  "quantity": 1,
  "price": 1999.99
}
```

**Curl Command**:
```bash
curl -X POST http://localhost:8081/api/orders ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"user-001\",\"productCode\":\"PROD-MACBOOK-M3\",\"quantity\":1,\"price\":1999.99}"
```

### 4.2. Quan sát Log & Kafka UI

1. **Log Console ứng dụng**:
   - `OrderProducerService` ghi log: Gửi thành công `OrderEvent` với Partition và Offset.
   - `OrderConsumerService` ghi log: `@KafkaListener` bắt được message và in chi tiết đơn hàng.
2. **Kafka UI**:
   - Truy cập [http://localhost:8089](http://localhost:8089) -> Vào menu **Topics** -> Chọn **`order-events`** -> Tab **Messages** để thấy message dạng JSON đã được lưu trữ trên Kafka.
