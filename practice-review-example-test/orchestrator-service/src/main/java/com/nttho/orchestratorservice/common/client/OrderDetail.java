package com.nttho.orchestratorservice.common.client;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    private Long id;
    private Long productId;
    private double price;
    private int quantity;

    private Order order;
}