package com.example.TTECHT.entity.cart;

import com.example.TTECHT.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carts_items")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id", nullable = false)
    Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    Product product;

    @Column(name = "product_name", nullable = false)
    String productName;

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @Column(name = "selected_color")
    String selectedColor;

    @Column(name = "selected_size")
    String selectedSize;
}
