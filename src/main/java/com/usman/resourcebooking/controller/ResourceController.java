package com.usman.resourcebooking.controller;

import com.usman.resourcebooking.dto.request.ResourceCreateRequest;
import com.usman.resourcebooking.dto.response.ApiResponse;
import com.usman.resourcebooking.dto.response.ResourceResponse;
import com.usman.resourcebooking.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ResourceResponse>>> getAllResources(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(resourceService.getAllResources(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(resourceService.getResourceById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> createResource(
            @Valid @RequestBody ResourceCreateRequest request) {
        ResourceResponse resource = resourceService.createResource(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource created successfully", resource));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Resource updated successfully",
                resourceService.updateResource(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.ok(ApiResponse.success("Resource deleted successfully"));
    }
}
