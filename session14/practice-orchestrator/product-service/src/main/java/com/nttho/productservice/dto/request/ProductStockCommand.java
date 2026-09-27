package com.nttho.productservice.dto.request;

import lombok.*;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ProductStockCommand {
    private Long id;
    private int stock;
}
