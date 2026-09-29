package com.nttho.orderservice.common.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product {
    private Long id;

    private String name;
    private double price;
    private int quantity;
}
