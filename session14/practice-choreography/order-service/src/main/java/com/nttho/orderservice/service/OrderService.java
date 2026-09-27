package com.nttho.orderservice.service;

import com.nttho.orderservice.client.ProductClient;
import com.nttho.orderservice.model.constrants.OrderStatus;
import com.nttho.orderservice.model.dto.kafka.order.OrderDetailKafka;
import com.nttho.orderservice.model.dto.kafka.order.OrderEventKafka;
import com.nttho.orderservice.model.dto.kafka.product.ProductReservationFailedEvent;
import com.nttho.orderservice.model.dto.kafka.product.ProductReservationSuccess;
import com.nttho.orderservice.model.dto.request.OrderRequest;
import com.nttho.orderservice.model.dto.request.ProductDto;
import com.nttho.orderservice.model.dto.response.OrderResponse;
import com.nttho.orderservice.model.entity.Order;
import com.nttho.orderservice.model.entity.OrderDetail;
import com.nttho.orderservice.repository.OrderDetailRepository;
import com.nttho.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    // findall, findbyid, createorder(OrderRequest)
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderDetailRepository orderDetailRepository;
    private final KafkaTemplate<String, OrderEventKafka> kafkaTemplate;

    // create order
    public OrderResponse createOrder(OrderRequest orderRequest){
        Order order = Order.builder()
                .customerName(orderRequest.getCustomerName())
                .orderStatus(OrderStatus.PENDING)
                .build();
        orderRepository.save(order);
        if (orderRequest.getOrderDetailRequests() != null) {
            List<OrderDetail> orderDetails = orderRequest.getOrderDetailRequests().stream()
                    .map(req -> OrderDetail.builder()
                            .order(order)
                            .productId(req.getProductId())
                            .quantity(req.getQuantity())
                            .build())
                    .toList();
            orderDetailRepository.saveAll(orderDetails);
        }

        // setup gửi sự kiện orderevent
        List<OrderDetailKafka> details = orderRequest.getOrderDetailRequests()
                .stream().map(detail -> OrderDetailKafka.builder()
                        .productId(detail.getProductId())
                        .quantity(detail.getQuantity())
                        .build()).toList();

        OrderEventKafka orderEventKafka = OrderEventKafka.builder()
                .orderDetailKafkas(details)
                .orderId(order.getId())
                .build();
        // send event to product service, kafkalistener sẽ nhận sự kiện này và xữ lý gửi laij sự kiện kiểm tra thành công
        kafkaTemplate.send("order-created", order.getId().toString(), orderEventKafka);

//        double total = 0.0;
//        if (orderRequest.getOrderDetailRequests() != null && !orderRequest.getOrderDetailRequests().isEmpty()){
//            for (OrderDetailRequest od : orderRequest.getOrderDetailRequests()) {
//                ProductDto product = productClient.findById(od.getProductId());
//                OrderDetail orderDetail = OrderDetail.builder()
//                        .order(order)
//                        .productId(product.getId())
//                        .quantity(od.getQuantity())
//                        .unitPrice(product.getPrice())
//                        .build();
//                orderDetailRepository.save(orderDetail);
//
//                total += product.getPrice() * od.getQuantity();
//            }
//        }
//
//        order.setTotal(total);
//        orderRepository.save(order);
//
//        OrderKafka orderKafka = OrderKafka.builder()
//                .id(order.getId())
//                .orderDetailKafkas(
//                        orderDetailRepository.findAllByOrder(order).stream()
//                                .map(detail -> OrderDetailKafka.builder()
//                                        .productId(detail.getProductId())
//                                        .quantity(detail.getQuantity())
//                                        .build())
//                                .toList()
//                )
//                .build();
//        kafkaTemplate.send("order-created", order.getId().toString(), orderKafka);

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .build();
    }

    // product service kiểm tra thành công order detail -> product tồn tại và đủ số lượng
    @KafkaListener(topics = "product-reservation-success")
    @Transactional
    public void handleProductReservationSuccess(OrderEventKafka productReservation){
        Order order = orderRepository.findById(productReservation.getOrderId()).orElseThrow();
        order.setOrderStatus(OrderStatus.COMPLETED);

        double total = 0.0;
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrder(order);
        for (OrderDetail orderDetail : orderDetails){
            // Gọi qua Feign lấy đúng theo productId
            ProductDto productDto = productClient.findById(orderDetail.getProductId());
            orderDetail.setUnitPrice(productDto.getPrice());
            orderDetailRepository.save(orderDetail);
            total += productDto.getPrice() * orderDetail.getQuantity();
        }
        order.setTotal(total);
        orderRepository.save(order);
    }

    @KafkaListener(topics = "product-reservation-failed")
    @Transactional
    public void handleProductReservationFailed(ProductReservationFailedEvent productReservationFailedEvent){
        Order order = orderRepository.findById(productReservationFailedEvent.getOrderId()).orElseThrow();
        order.setOrderStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }

    public List<Order> findAll(){
        return orderRepository.findAll();
    }

    public OrderResponse findById(Long id){
        Order order = orderRepository.findById(id).orElseThrow();

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .total(order.getTotal())
                .orderDetails(
                        orderDetailRepository.findAllByOrder(order)
                )
                .build();
    }
}
