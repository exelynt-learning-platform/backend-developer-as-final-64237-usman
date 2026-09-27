package com.usman.resourcebooking.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.usman.resourcebooking.dto.request.ResourceCreateRequest;
import com.usman.resourcebooking.dto.response.ResourceResponse;
import com.usman.resourcebooking.exception.ResourceNotFoundException;
import com.usman.resourcebooking.model.Resource;
import com.usman.resourcebooking.repository.ResourceRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceServiceImpl Unit Tests")
class ResourceServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    private Resource resource;
    private ResourceCreateRequest request;

    @BeforeEach
    void setUp() {
        resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .description("A large room")
                .type("ROOM")
                .available(true)
                .build();

        request = new ResourceCreateRequest();
        request.setName("Conference Room");
        request.setDescription("A large room");
        request.setType("ROOM");
        request.setAvailable(true);
    }

    @Test
    @DisplayName("Get all resources - returns paged results")
    void getAllResources_ReturnsPagedResults() {
        Page<Resource> pagedResources = new PageImpl<>(List.of(resource));
        when(resourceRepository.findAll(any(Pageable.class))).thenReturn(pagedResources);

        Page<ResourceResponse> response = resourceService.getAllResources(Pageable.unpaged());

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Conference Room", response.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Get resource by id - found")
    void getResourceById_Found() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        ResourceResponse response = resourceService.getResourceById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Conference Room", response.getName());
    }

    @Test
    @DisplayName("Get resource by id - not found")
    void getResourceById_NotFound() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceById(1L));
    }

    @Test
    @DisplayName("Create resource")
    void createResource() {
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

        ResourceResponse response = resourceService.createResource(request);

        assertNotNull(response);
        assertEquals("Conference Room", response.getName());
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    @DisplayName("Update resource - found")
    void updateResource_Found() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

        request.setName("Updated Room");
        ResourceResponse response = resourceService.updateResource(1L, request);

        assertNotNull(response);
        verify(resourceRepository, times(1)).save(resource);
    }

    @Test
    @DisplayName("Delete resource - found")
    void deleteResource_Found() {
        when(resourceRepository.existsById(1L)).thenReturn(true);

        resourceService.deleteResource(1L);

        verify(resourceRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete resource - not found")
    void deleteResource_NotFound() {
        when(resourceRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> resourceService.deleteResource(1L));
        verify(resourceRepository, never()).deleteById(anyLong());
    }
}
