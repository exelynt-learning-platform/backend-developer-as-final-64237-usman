package com.usman.resourcebooking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReservationCreateRequest {

    @NotNull(message = "Resource ID is required")
    private Long resourceId;

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be in the present or future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be zero or greater")
    private BigDecimal price;
}
