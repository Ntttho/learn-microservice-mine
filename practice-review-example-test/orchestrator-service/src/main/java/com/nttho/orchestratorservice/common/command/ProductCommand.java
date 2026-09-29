package com.nttho.orchestratorservice.common.command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductCommand {
    private Long productId;
    private int quantity;
}
