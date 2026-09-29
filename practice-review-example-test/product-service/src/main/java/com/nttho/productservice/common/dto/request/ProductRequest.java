package com.nttho.productservice.common.dto.request;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private double price;
    private int quantity;
}
