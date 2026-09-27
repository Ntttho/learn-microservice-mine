package com.nttho.orderservice.model.dto.kafka.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductReservationFailedEvent {
    private Long orderId;
    private String reason;
}