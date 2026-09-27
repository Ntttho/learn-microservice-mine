package com.nttho.orderservice.controller;

import com.nttho.orderservice.model.OrderStatus;
import com.nttho.orderservice.model.dto.request.OrderRequest;
import com.nttho.orderservice.model.dto.response.OrderResponse;
import com.nttho.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    @GetMapping
    public List<OrderResponse> findAll(){
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id){
        return orderService.findById(id);
    }

    @PostMapping("/create-order")
    public OrderResponse createOrder(@RequestBody OrderRequest request){
        return orderService.createOrder(request);
    }

    @PostMapping("/{id}/update-status")
    public OrderResponse updateOrder(@PathVariable Long id, @RequestParam(name = "status") OrderStatus status){
        return orderService.updateStatusOrder(id, status);
    }

}
