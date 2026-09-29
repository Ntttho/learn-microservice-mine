package com.nttho.orderservice.common.dto.response;


import com.nttho.orderservice.entity.Order;
import com.nttho.orderservice.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderResponse {
    Order order;
    List<OrderDetail> orderDetails;
}
