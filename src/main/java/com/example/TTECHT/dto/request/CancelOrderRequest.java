package com.example.TTECHT.dto.request;

import com.example.TTECHT.enumuration.CancellationReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CancelOrderRequest {
    
    @NotNull(message = "Cancellation reason is required")
    private CancellationReason cancellationReason;
    
    private String cancelledBy; // Optional: could be system, admin, or customer
}
