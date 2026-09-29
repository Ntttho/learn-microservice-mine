package com.nttho.notificationservice.service;

import com.nttho.notificationservice.common.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    @KafkaListener(topics = "order-update", groupId = "notification-group")
    public void notification(OrderEvent orderEvent){
        log.info("notification for" +  orderEvent.getUsername() + "your order: " + orderEvent.getOrder_id() + " is " +
                orderEvent.getOrderStatus().toString()
        );
    }
}
