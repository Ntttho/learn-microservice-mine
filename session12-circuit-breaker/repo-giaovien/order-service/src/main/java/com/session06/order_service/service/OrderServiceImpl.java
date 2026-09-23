package com.session06.order_service.service;

import com.session06.order_service.client.ProductServiceClient;
import com.session06.order_service.dto.*;
import com.session06.order_service.model.Order;
import com.session06.order_service.model.OrderItem;
import com.session06.order_service.model.OrderStatus;
import com.session06.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final ProductServiceClient productServiceClient;
    private final OrderRepository orderRepository;

    @Override
    public List<ProductDto> getAvailableProducts() {
        log.info("Fetching available products catalog from Product Service...");
        return productServiceClient.getAllProducts();
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Processing new order creation for customer: {}", request.getCustomerName());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .id(orderId)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerAddress(request.getCustomerAddress())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.CONFIRMED)
                .build();

        // Verify products and calculate total using RestTemplate results from product-service
        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductDto product = productServiceClient.getProductById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(unitPrice)
                    .quantity(itemRequest.getQuantity())
                    .subTotal(subTotal)
                    .order(order)
                    .build();

            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created and saved to MySQL 'order-db' successfully with ID: {}, Total amount: {}", orderId, totalAmount);

        return mapToOrderResponse(savedOrder, "Order placed successfully and persisted to MySQL database");
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders from MySQL database...");
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(order -> mapToOrderResponse(order, null))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderResponse> getOrderById(String id) {
        log.info("Fetching order by ID: {} from MySQL database...", id);
        return orderRepository.findById(id)
                .map(order -> mapToOrderResponse(order, null));
    }

    private OrderResponse mapToOrderResponse(Order order, String customMessage) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subTotal(item.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerAddress(order.getCustomerAddress())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .message(customMessage)
                .build();
    }
}
