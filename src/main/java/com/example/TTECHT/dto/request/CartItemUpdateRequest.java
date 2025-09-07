package com.example.TTECHT.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemUpdateRequest {
    private Integer quantity;
    private String color;
    private String size;
}
