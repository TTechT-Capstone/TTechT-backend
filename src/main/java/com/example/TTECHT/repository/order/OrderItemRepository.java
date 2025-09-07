package com.example.TTECHT.repository.order;

import java.util.List;

import com.example.TTECHT.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TTECHT.entity.order.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder_OrderId(Long orderId);
    List<OrderItem> findByOrderAndProductSellerId(Order order, Long sellerId);
}
