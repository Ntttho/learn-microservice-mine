package com.nttho.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {
    private String orderId;
    private String userId;
    private String productCode;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String status; // e.g. "CREATED", "PENDING", "COMPLETED"
    private LocalDateTime timestamp;
}
