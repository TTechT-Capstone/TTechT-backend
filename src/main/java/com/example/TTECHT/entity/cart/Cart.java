package com.example.TTECHT.entity.cart;


import com.example.TTECHT.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carts")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // consider nullable = false for mandatory fields
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "promotion_code", nullable = true)
    String promotionCode;

    @Column(name = "submitted_time")
    LocalDateTime submittedTime;

    @Column(name = "created_at")
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    LocalDateTime updatedAt;


}
