package com.usman.resourcebooking.service;

import com.usman.resourcebooking.dto.request.ResourceCreateRequest;
import com.usman.resourcebooking.dto.response.ResourceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contract for resource management operations.
 */
public interface ResourceService {

    Page<ResourceResponse> getAllResources(Pageable pageable);

    ResourceResponse getResourceById(Long id);

    ResourceResponse createResource(ResourceCreateRequest request);

    ResourceResponse updateResource(Long id, ResourceCreateRequest request);

    void deleteResource(Long id);
}
