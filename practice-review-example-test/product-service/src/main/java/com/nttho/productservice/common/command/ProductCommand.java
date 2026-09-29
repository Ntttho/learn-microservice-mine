package com.nttho.productservice.common.command;

import lombok.Data;

@Data
public class ProductCommand {
    private Long productId;
    private int quantity;
}
