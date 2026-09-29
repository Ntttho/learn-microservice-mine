package com.nttho.orderservice.entity;

import com.nttho.orderservice.common.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private double total;
    private LocalDate created = LocalDate.now();
    private OrderStatus orderStatus;
}
