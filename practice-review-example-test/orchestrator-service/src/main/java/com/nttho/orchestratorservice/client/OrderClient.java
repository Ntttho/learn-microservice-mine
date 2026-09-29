package com.nttho.orchestratorservice.client;

import com.nttho.orchestratorservice.common.OrderStatus;
import com.nttho.orchestratorservice.common.client.Order;
import com.nttho.orchestratorservice.common.dto.request.OrderRequest;
import com.nttho.orchestratorservice.common.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderClient {
    @GetMapping
    public List<OrderResponse> getAll();

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id);

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request);

    @PutMapping("/{id}")
    public Order updateStatus(@PathVariable Long id, @RequestParam OrderStatus status);
}
