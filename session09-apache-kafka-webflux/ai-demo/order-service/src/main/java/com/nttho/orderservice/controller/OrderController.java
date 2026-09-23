package com.nttho.orderservice.controller;

import com.nttho.orderservice.dto.OrderEvent;
import com.nttho.orderservice.dto.OrderRequest;
import com.nttho.orderservice.service.OrderProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderProducerService orderProducerService;

    @PostMapping
    public ResponseEntity<OrderEvent> createOrder(@RequestBody OrderRequest request) {
        BigDecimal totalPrice = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        
        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .productCode(request.getProductCode())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .totalPrice(totalPrice)
                .status("CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        orderProducerService.sendOrderEvent(orderEvent);

        return ResponseEntity.ok(orderEvent);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Order Service is running and connected to Kafka!");
    }
}
