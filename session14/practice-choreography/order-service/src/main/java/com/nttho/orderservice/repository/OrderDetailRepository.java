package com.nttho.orderservice.repository;

import com.nttho.orderservice.model.entity.Order;
import com.nttho.orderservice.model.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findAllByOrder(Order order);
}
