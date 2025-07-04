package com.example.TTECHT.mapper;

import com.example.TTECHT.dto.repsonse.CartResponse;
import com.example.TTECHT.dto.request.CartCreationRequest;
import com.example.TTECHT.entity.cart.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {
    Cart toCart(CartCreationRequest request);

    // Convert Cart entity to CartResponse DTO
    CartResponse toCartResponse(Cart cart);
}
