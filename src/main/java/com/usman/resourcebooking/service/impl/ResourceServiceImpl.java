package com.usman.resourcebooking.service.impl;

import com.usman.resourcebooking.dto.request.ResourceCreateRequest;
import com.usman.resourcebooking.dto.response.ResourceResponse;
import com.usman.resourcebooking.exception.ResourceNotFoundException;
import com.usman.resourcebooking.model.Resource;
import com.usman.resourcebooking.repository.ResourceRepository;
import com.usman.resourcebooking.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ResourceResponse> getAllResources(Pageable pageable) {
        return resourceRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ResourceResponse createResource(ResourceCreateRequest request) {
        Resource resource = Resource.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .available(request.isAvailable())
                .build();
        return mapToResponse(resourceRepository.save(resource));
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long id, ResourceCreateRequest request) {
        Resource resource = findOrThrow(id);

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setAvailable(request.isAvailable());

        return mapToResponse(resourceRepository.save(resource));
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource", "id", id);
        }
        resourceRepository.deleteById(id);
    }

    private Resource findOrThrow(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));
    }

    private ResourceResponse mapToResponse(Resource r) {
        return ResourceResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .type(r.getType())
                .available(r.isAvailable())
                .build();
    }
}
