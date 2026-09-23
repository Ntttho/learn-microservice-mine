package com.nttho.orderservice.service;

import com.nttho.orderservice.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.order-events:order-events}")
    private String orderEventsTopic;

    public CompletableFuture<SendResult<String, Object>> sendOrderEvent(OrderEvent orderEvent) {
        log.info("Sending OrderEvent to topic '{}': {}", orderEventsTopic, orderEvent);
        
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(orderEventsTopic, orderEvent.getOrderId(), orderEvent);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent OrderEvent [id={}] with offset=[{}] to partition=[{}]",
                        orderEvent.getOrderId(),
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send OrderEvent [id={}] due to: {}",
                        orderEvent.getOrderId(), ex.getMessage(), ex);
            }
        });

        return future;
    }
}
