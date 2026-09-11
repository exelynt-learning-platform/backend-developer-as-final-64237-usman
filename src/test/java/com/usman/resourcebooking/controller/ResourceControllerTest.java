package com.usman.resourcebooking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usman.resourcebooking.dto.request.ResourceCreateRequest;
import com.usman.resourcebooking.dto.response.ResourceResponse;
import com.usman.resourcebooking.model.Role;
import com.usman.resourcebooking.model.User;
import com.usman.resourcebooking.security.UserPrincipal;
import com.usman.resourcebooking.service.ResourceService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ResourceController Integration Tests")
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResourceService resourceService;

    private UsernamePasswordAuthenticationToken userAuth() {
        User user = User.builder().id(1L).username("user1").role(Role.USER).build();
        UserPrincipal principal = UserPrincipal.create(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken adminAuth() {
        User admin = User.builder().id(2L).username("admin").role(Role.ADMIN).build();
        UserPrincipal principal = UserPrincipal.create(admin);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private ResourceCreateRequest validRequest() {
        ResourceCreateRequest req = new ResourceCreateRequest();
        req.setName("Projector");
        req.setDescription("HD Projector");
        req.setType("EQUIPMENT");
        req.setAvailable(true);
        return req;
    }

    private ResourceResponse sampleResponse() {
        return ResourceResponse.builder()
                .id(1L)
                .name("Projector")
                .description("HD Projector")
                .type("EQUIPMENT")
                .available(true)
                .build();
    }

    @Test
    @DisplayName("GET /resources - Any authenticated user can list resources")
    void getAllResources_Authenticated_Returns200() throws Exception {
        org.springframework.data.domain.Page<ResourceResponse> page = new org.springframework.data.domain.PageImpl<>(
                Collections.singletonList(sampleResponse()));
        given(resourceService.getAllResources(any(org.springframework.data.domain.Pageable.class))).willReturn(page);

        mockMvc.perform(get("/resources")
                .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("POST /resources - ADMIN can create resource")
    void createResource_Admin_Returns201() throws Exception {
        given(resourceService.createResource(any())).willReturn(sampleResponse());

        mockMvc.perform(post("/resources")
                .with(authentication(adminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Projector"));
    }

    @Test
    @DisplayName("POST /resources - USER gets 403 Forbidden")
    void createResource_User_Returns403() throws Exception {
        mockMvc.perform(post("/resources")
                .with(authentication(userAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /resources/{id} - ADMIN can update resource")
    void updateResource_Admin_Returns200() throws Exception {
        given(resourceService.updateResource(eq(1L), any())).willReturn(sampleResponse());

        mockMvc.perform(put("/resources/1")
                .with(authentication(adminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /resources/{id} - USER gets 403 Forbidden")
    void updateResource_User_Returns403() throws Exception {
        mockMvc.perform(put("/resources/1")
                .with(authentication(userAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /resources/{id} - ADMIN can delete resource")
    void deleteResource_Admin_Returns200() throws Exception {
        mockMvc.perform(delete("/resources/1")
                .with(authentication(adminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /resources/{id} - USER gets 403 Forbidden")
    void deleteResource_User_Returns403() throws Exception {
        mockMvc.perform(delete("/resources/1")
                .with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }
}
