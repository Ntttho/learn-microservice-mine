package com.nttho.orderservice.service;

import com.nttho.orderservice.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderConsumerService {

    @KafkaListener(
            topics = "${app.kafka.topic.order-events:order-events}",
            groupId = "${spring.kafka.consumer.group-id:order-group}"
    )
    public void consumeOrderEvent(ConsumerRecord<String, OrderEvent> record) {
        log.info("Received Kafka message -> Key: {}, Partition: {}, Offset: {}, Value: {}",
                record.key(),
                record.partition(),
                record.offset(),
                record.value());

        OrderEvent event = record.value();
        log.info("Processing order: ID={}, Product={}, TotalPrice={}",
                event.getOrderId(),
                event.getProductCode(),
                event.getTotalPrice());
    }
}
