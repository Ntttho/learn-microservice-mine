package com.nttho.orderservice.service;

import com.nttho.orderservice.client.ProductFeign;
import com.nttho.orderservice.common.OrderStatus;
import com.nttho.orderservice.common.client.Product;
import com.nttho.orderservice.common.dto.request.OrderDetailRequest;
import com.nttho.orderservice.common.dto.request.OrderRequest;
import com.nttho.orderservice.common.dto.response.OrderResponse;
import com.nttho.orderservice.common.event.OrderEvent;
import com.nttho.orderservice.entity.Order;
import com.nttho.orderservice.entity.OrderDetail;
import com.nttho.orderservice.repository.OrderDetailRepository;
import com.nttho.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductFeign productFeign;

    public OrderResponse createOrder(OrderRequest request){

        Order order = new Order();
        order.setOrderStatus(OrderStatus.PENDING);
        order.setCreated(LocalDate.now());
        Order orderSave = orderRepository.save(order);
        double total = 0.0;
        for (OrderDetailRequest detailRequest : request.getOrderDetailRequest()){

            // update thong tin order detail and save
            Product product = productFeign.findById(detailRequest.getProductId());

            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(detailRequest.getProductId())
                    .quantity(detailRequest.getQuantity())
                    .order(order)
                    .price(product.getPrice())
                    .build();
            
            total += product.getPrice() * detailRequest.getQuantity();
            orderDetailRepository.save(orderDetail);
        }

        // cap nhap gia tong don hang
        orderSave.setTotal(total);
        orderRepository.save(orderSave);

        return OrderResponse.builder()
                .order(orderSave)
                .orderDetails(orderDetailRepository.findByOrder(orderSave))
                .build();
    }

    public Order updateStatus(Long id, OrderStatus status){
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order find not found"));
        order.setOrderStatus(status);

        kafkaTemplate.send("order-update", OrderEvent.builder().order_id(id).orderStatus(status).build() );


        return orderRepository.save(order);
    }


    public OrderResponse findById(Long id){
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);

        return OrderResponse.builder()
                .order(order)
                .orderDetails(orderDetails)
                .build();
    }

    public List<OrderResponse> findAllOrderResponses(){
        List<OrderResponse> orderResponses = new ArrayList<>();
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders){
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
            OrderResponse orderResponse = OrderResponse.builder()
                    .order(order)
                    .orderDetails(orderDetails)
                    .build();
            orderResponses.add(orderResponse);
        }
        return orderResponses;
    }
}
