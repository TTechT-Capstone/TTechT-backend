package com.example.TTECHT.mapper;

import com.example.TTECHT.dto.repsonse.CartResponse;
import com.example.TTECHT.dto.request.CartCreationRequest;
import com.example.TTECHT.entity.cart.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "submittedTime", ignore = true)
    @Mapping(target = "user", ignore = true)
    Cart toCart(CartCreationRequest request);

    // Convert Cart entity to CartResponse DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "submittedTime", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "userId", ignore = true)
    CartResponse toCartResponse(Cart cart);
}
