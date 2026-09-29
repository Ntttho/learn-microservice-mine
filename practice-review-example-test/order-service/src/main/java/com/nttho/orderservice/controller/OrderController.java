package com.nttho.orderservice.controller;

import com.nttho.orderservice.common.OrderStatus;
import com.nttho.orderservice.common.dto.request.OrderRequest;
import com.nttho.orderservice.common.dto.response.OrderResponse;
import com.nttho.orderservice.entity.Order;
import com.nttho.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getAll(){
        return orderService.findAllOrderResponses();
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id){
        return orderService.findById(id);
    }

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request){
        return orderService.createOrder(request);
    }

    @PutMapping("/{id}")
    public Order updateStatus(@PathVariable Long id, @RequestParam OrderStatus status){
        return orderService.updateStatus(id, status);
    }

}
