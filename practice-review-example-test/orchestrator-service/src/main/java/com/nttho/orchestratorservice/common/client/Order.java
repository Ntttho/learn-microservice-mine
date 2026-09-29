package com.nttho.orchestratorservice.common.client;

import com.nttho.orchestratorservice.common.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Builder
public class Order {

    private Long id;
    private double total;
    private LocalDate created = LocalDate.now();
    private OrderStatus orderStatus;
}
