package com.nttho.productservice.service;

import com.nttho.productservice.dto.kafka.order.OrderDetailKafka;
import com.nttho.productservice.dto.kafka.order.OrderEventKafka;
import com.nttho.productservice.dto.kafka.order.OrderKafka;
import com.nttho.productservice.dto.kafka.product.ProductReservationFailedEvent;
import com.nttho.productservice.entity.Product;
import com.nttho.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProductRepository productRepository;
    @KafkaListener(topics = "order-created")
    @Transactional
    public void handleOrderCreate(OrderEventKafka orderEventKafka){

        try{
            for (OrderDetailKafka detail : orderEventKafka.getOrderDetailKafkas()){
                Product product = productRepository
                        .findById(detail.getProductId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found: "
                                                + detail.getProductId()
                                )
                        );
                if (product.getStock() < detail.getQuantity()){
                    throw new RuntimeException("Not enough quantity");
                }
                product.setStock(product.getStock() - detail.getQuantity());
                productRepository.save(product);

            }
            kafkaTemplate.send(
                    "product-reservation-success",
                    orderEventKafka.getOrderId().toString(),
                    orderEventKafka
            );
        }catch (Exception e){
            // hoan tac rollback
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            ProductReservationFailedEvent failedEvent =
                    ProductReservationFailedEvent.builder()
                            .orderId(orderEventKafka.getOrderId())
                            .reason(e.getMessage())
                            // product not found || not enough quantity
                            .build();

            // bất kỳ lỗi nào xẫy ra cũng có thể gọi product-reservation-failed để thực hiện compenvation
            kafkaTemplate.send(
                    "product-reservation-failed",
                    orderEventKafka.getOrderId().toString(),
                failedEvent
            );
        }
    }
}
