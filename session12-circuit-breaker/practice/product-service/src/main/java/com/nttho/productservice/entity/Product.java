package com.nttho.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product {
    private String id = UUID.randomUUID().toString();
    private String name;
    private double price;
    private int amount;
    // more attribute
}
