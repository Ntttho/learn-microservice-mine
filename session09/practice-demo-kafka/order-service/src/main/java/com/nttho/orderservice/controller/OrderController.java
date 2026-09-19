package com.nttho.orderservice.controller;

import com.nttho.orderservice.dto.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")

public class OrderController {
    private final List<OrderResponse> orderResponses = new ArrayList<>();
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping
    public List<OrderResponse> findAll(){
        return orderResponses;
    }

    @PostMapping("/create")
    public OrderResponse createOrder(){
        String id = UUID.randomUUID().toString();

        OrderResponse orderResponse = new OrderResponse(id, "product " + id, id.substring(1,5) + "@gmail.com");
        orderResponses.add(orderResponse);

        // gửi cho kafka biết rằng order đã được tạo (notification-service sẽ cấu nhận tin nhắn của order gửi tin nhắn)
        kafkaTemplate.send(
//                        new TopicBuilder.name("order-email"), // cấu hình thành bean topicbuilder
                "order-notification",
//                        4, // partition -> số lượng tối đa consumer connect tới
//                         ... có thể cấu hình topic bằng TopicBuilder
                orderResponse.getId(),
                orderResponse
        )
                // check gửi data cho kafka biết thành công chưa hay là thất bại rồi
                .whenComplete(
                        (result, ex) ->
                        {
                            if (ex == null){
                                System.out.println("sent message offset" + result.getRecordMetadata());
                            }else {
                                System.err.println("failed sent message" + ex.getMessage());
                            }
                        }
                );
        return orderResponse;
    }
}
