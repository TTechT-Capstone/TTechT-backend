package com.example.TTECHT.repository.order;

import com.example.TTECHT.entity.order.Order;
import com.example.TTECHT.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUser(User user);
}
