package com.usman.resourcebooking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        private static final String SECURITY_SCHEME_NAME = "bearerAuth";

        @Bean
        public OpenAPI resourceBookingOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Resource Booking API")
                                                .version("1.0")
                                                .description("Secure RESTful API for Resource Booking with JWT Authentication "
                                                                + "and Role-Based Access Control."))
                                .components(new Components()
                                                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                                                .name(SECURITY_SCHEME_NAME)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")))
                                .addSecurityItem(new SecurityRequirement()
                                                .addList(SECURITY_SCHEME_NAME));
        }
}
