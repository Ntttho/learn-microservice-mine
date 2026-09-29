package com.nttho.orderservice.common.event;

import com.nttho.orderservice.common.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class OrderEvent {
    private Long order_id;
    private String username = "admin";
    private OrderStatus orderStatus;
}
