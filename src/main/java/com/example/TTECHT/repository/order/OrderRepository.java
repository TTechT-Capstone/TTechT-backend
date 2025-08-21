package com.example.TTECHT.repository.order;

import com.example.TTECHT.entity.order.Order;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.enumuration.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi " +
            "WHERE oi.product.seller.id = :sellerId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrdersForSellerWithFilters(
            @Param("sellerId") Long sellerId
    );
}
