package com.nttho.notifycationservice.kafka;

import com.nttho.notifycationservice.controller.NotifycationController;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaConsumer {

    @KafkaListener(
            topics = "order-email",
            groupId = "notification-service"
    )
    public void consumerOrderCreated(String object){
        NotifycationController.emails.add(object);
        System.out.println("Received order message: " + object);
    }
}
