package com.nttho.orchestratorsaga.client;

import com.nttho.orchestratorsaga.model.constants.OrderStatus;
import com.nttho.orchestratorsaga.model.dto.request.OrderRequest;
import com.nttho.orchestratorsaga.model.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderClient {

    @PostMapping("/create-order")
    public OrderResponse createOrder(@RequestBody OrderRequest request);

    @PostMapping("/{id}/update-status")
    public OrderResponse updateOrder(@PathVariable Long id, @RequestParam(name = "status") OrderStatus status);
}