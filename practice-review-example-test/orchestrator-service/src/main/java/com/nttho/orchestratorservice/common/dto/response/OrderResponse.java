package com.nttho.orchestratorservice.common.dto.response;



import com.nttho.orchestratorservice.common.client.Order;
import com.nttho.orchestratorservice.common.client.OrderDetail;
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
