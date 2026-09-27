package com.nttho.orderservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nttho.orderservice.entity.OrderDetail;
import com.nttho.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String customerName;
    private double total;
    private OrderStatus orderStatus;
    @JsonIgnoreProperties({"order"})
    private List<OrderDetail> orderDetails;
}
