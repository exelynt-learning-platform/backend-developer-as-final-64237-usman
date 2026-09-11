package com.usman.resourcebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceCreateRequest {

    @NotBlank(message = "Resource name is required")
    private String name;

    private String description;

    private String type;

    private boolean available = true;
}
