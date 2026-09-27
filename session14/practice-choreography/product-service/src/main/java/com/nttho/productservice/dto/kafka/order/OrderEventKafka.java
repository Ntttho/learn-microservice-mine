package com.nttho.productservice.dto.kafka.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderEventKafka {

    private Long orderId;
    private List<OrderDetailKafka> orderDetailKafkas;
}
