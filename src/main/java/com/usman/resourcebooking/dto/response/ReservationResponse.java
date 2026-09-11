package com.usman.resourcebooking.dto.response;

import com.usman.resourcebooking.model.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationResponse {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    private Long resourceId;
    private String resourceName;
    private String resourceType;

    private Long userId;
    private String username;
}
