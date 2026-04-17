package com.huseyinsacikay.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI footballReservationOpenApi() {
        // Global security requirement - defined only in root level
        SecurityRequirement globalSecurityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI(SpecVersion.V30)
                .openapi("3.0.1")
                .info(new Info()
                        .title("Football Reservation API")
                        .version("v1.0.0")
                        .description("""
                                Professional REST API for football pitch reservations and team management.

                                **Key Features:**
                                - User authentication with JWT tokens
                                - Multi-user reservation support for team bookings
                                - Pitch inventory management (Admin only)
                                - User profile management
                                - Responsive error handling with typed error codes

                                **Authentication:**
                                All protected endpoints require a JWT token from /api/v1/auth/login or /api/v1/auth/register.
                                Include tokens in the Authorization header: Authorization: Bearer <your_jwt_token>

                                **Error Handling:**
                                The API returns structured error responses with:
                                - HTTP status code
                                - Error code (numeric identifier)
                                - Human-readable message
                                - Request path and timestamp

                                Use the generated OpenAPI JSON for Postman/Insomnia imports and Swagger UI for interactive testing.
                                """)
                        .contact(new Contact()
                                .name("Huseyin Sacikay")
                                .url("https://github.com/huseyinsc")
                                .email("huseyinsacikay0@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer token obtained from authentication endpoints. Format: Bearer <token>"))
                        .addSchemas("ValidationApiError", createValidationApiErrorSchema())
                        .addSchemas("StringApiError", createStringApiErrorSchema())
                )
                .addSecurityItem(globalSecurityRequirement);  // only once in root level
    }

    @Bean
    public GlobalOpenApiCustomizer openApi30Customizer() {
        return openApi -> {
            openApi.setSpecVersion(SpecVersion.V30);
            openApi.setOpenapi("3.0.1");

            // Add global error responses to all operations
            openApi.getPaths().values().forEach(pathItem -> {
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getResponses() == null) {
                        operation.setResponses(new ApiResponses());
                    }

                    // Add common error responses if not already present
                    if (!operation.getResponses().containsKey("400")) {
                        operation.getResponses().addApiResponse("400",
                                new ApiResponse()
                                        .description("Validation failed")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema().$ref("#/components/schemas/ValidationApiError")))));
                    }
                    if (!operation.getResponses().containsKey("401")) {
                        operation.getResponses().addApiResponse("401",
                                new ApiResponse()
                                        .description("Authentication required")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema().$ref("#/components/schemas/StringApiError")))));
                    }
                    if (!operation.getResponses().containsKey("403")) {
                        operation.getResponses().addApiResponse("403",
                                new ApiResponse()
                                        .description("Access denied")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema().$ref("#/components/schemas/StringApiError")))));
                    }
                    if (!operation.getResponses().containsKey("500")) {
                        operation.getResponses().addApiResponse("500",
                                new ApiResponse()
                                        .description("Internal server error")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema().$ref("#/components/schemas/StringApiError")))));
                    }
                    
                    // CRITICAL: Remove security requirements from sub-paths
                    // This way they appear as "inherit auth from parent" in Postman
                    operation.setSecurity(null);
                });
            });
        };
    }

    private Schema<?> createValidationApiErrorSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("status", new Schema<>().type("integer").example(400))
                .addProperty("exception", new Schema<>()
                        .type("object")
                        .addProperty("code", new Schema<>().type("string").example("1009"))
                        .addProperty("path", new Schema<>().type("string").example("/api/v1/auth/register"))
                        .addProperty("createTime", new Schema<>().type("string").format("date-time").example("2026-04-10T16:40:11.406+03:00"))
                        .addProperty("message", new Schema<>()
                                .type("object")
                                .example("{\"password\":\"Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)\",\"username\":\"Username must be between 3 and 20 characters\"}")));
    }

    private Schema<?> createStringApiErrorSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("status", new Schema<>().type("integer").example(401))
                .addProperty("exception", new Schema<>()
                        .type("object")
                        .addProperty("code", new Schema<>().type("string").example("1010"))
                        .addProperty("path", new Schema<>().type("string").example("/api/v1/auth/login"))
                        .addProperty("createTime", new Schema<>().type("string").format("date-time").example("2026-04-10T16:40:11.406+03:00"))
                        .addProperty("message", new Schema<>().type("string").example("Authentication is required to access this resource")));
    }
}