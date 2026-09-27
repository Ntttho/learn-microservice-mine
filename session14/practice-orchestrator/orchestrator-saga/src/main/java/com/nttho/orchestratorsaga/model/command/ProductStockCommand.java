package com.nttho.orchestratorsaga.model.command;

import lombok.*;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ProductStockCommand {
    private Long id;
    private int stock;
}
