package com.usman.resourcebooking.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private String type;
    private boolean available;
}
