package com.nttho.orchestratorsaga.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class OrderDetail {

    private Long id;
    private Long productId;
    private  int quantity;
    private double unitPrice;
}
