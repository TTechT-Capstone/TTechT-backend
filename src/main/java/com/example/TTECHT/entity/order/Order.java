package com.example.TTECHT.entity.order;



import com.example.TTECHT.entity.cart.Cart;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.enumuration.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number", length = 255)
    private String orderNumber;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "order_status", length = 255)
    private String orderStatus;

    @Column(name = "contact_name", length = 255)
    private String contactName;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 255)
    private String contactPhone;

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    @Column(name = "promotion_code", length = 255)
    private String promotionCode;

    @Column(name = "payment_method", length = 255)
    private PaymentMethod paymentMethod;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    private Cart cart;
}
