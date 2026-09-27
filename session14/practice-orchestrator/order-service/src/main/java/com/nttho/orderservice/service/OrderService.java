package com.nttho.orderservice.service;

import com.nttho.orderservice.client.ProductClient;
import com.nttho.orderservice.entity.Order;
import com.nttho.orderservice.entity.OrderDetail;
import com.nttho.orderservice.model.OrderStatus;
import com.nttho.orderservice.model.dto.request.OrderRequest;
import com.nttho.orderservice.model.dto.response.ProductDto;
import com.nttho.orderservice.model.dto.response.OrderResponse;
import com.nttho.orderservice.repository.OrderDetailRepository;
import com.nttho.orderservice.repository.OrderRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductClient productClient;


    // findall, findbyid, createorder, updatestatus

    public OrderResponse createOrder(OrderRequest request){
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .orderStatus(OrderStatus.PENDING)
                .build();

        Order orderSave = orderRepository.save(order);
        if(request.getOrderDetailRequests() != null && !request.getOrderDetailRequests().isEmpty()){

        request.getOrderDetailRequests()
                .forEach(detail -> {
                    ProductDto productDto = productClient.getProductById(detail.getProductId());
                    if (productDto != null){
                        OrderDetail orderDetail = OrderDetail.builder()
                                .order(orderSave)
                                .productId(productDto.getId())
                                .quantity(detail.getQuantity())
                                .unitPrice(productDto.getPrice())
                                .build();
                        orderDetailRepository.save(orderDetail);
                    }

                });
        }
        return OrderResponse.builder()
                .id(orderSave.getId())
                .orderStatus(orderSave.getOrderStatus())
                .orderDetails(orderDetailRepository.findByOrderId(orderSave.getId()))
                .build();
    }

    public OrderResponse updateStatusOrder(Long id, OrderStatus status){
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("order not found"));
        order.setOrderStatus(status);
        orderRepository.save(order);

        return OrderResponse.builder()
                .id(order.getId())
                .orderStatus(order.getOrderStatus())
                .orderDetails(orderDetailRepository.findByOrderId(order.getId()))
                .build();
    }

    public OrderResponse findById(@PathVariable Long id){
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException(""));
        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .orderDetails(orderDetailRepository.findByOrderId(order.getId()))
                .orderStatus(order.getOrderStatus())
                .build();
    }

    public List<OrderResponse> findAll(){
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();
        for(Order order: orders){
            OrderResponse orderResponse = OrderResponse.builder()
                    .id(order.getId())
                    .customerName(order.getCustomerName())
                    .orderDetails(orderDetailRepository.findByOrderId(order.getId()))
                    .orderStatus(order.getOrderStatus())
                    .build();
            orderResponses.add(orderResponse);
        }

        return orderResponses;
    }
}
