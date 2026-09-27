package com.example.api.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI/Swagger configuration with JWT security scheme support.
 */
@Configuration
class OpenApiConfig(
    @Value("\${api.version}") private val apiVersion: String,
    @Value("\${api.title}") private val apiTitle: String,
    @Value("\${api.description}") private val apiDescription: String
) {

    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title(apiTitle)
                    .version(apiVersion)
                    .description(apiDescription)
                    .contact(
                        Contact()
                            .name("Marco Vinícius")
                            .email("marcosvmdilly@gmail.com")
                            .url("https://github.com/mvdevelop")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList("Bearer Authentication")
            )
            .components(
                Components().addSecuritySchemes(
                    "Bearer Authentication",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter your JWT token in the format: Bearer {token}")
                )
            )

    @Bean
    fun openApiCustomizer(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
        openApi.paths?.removeIf { path -> path.key.startsWith("/actuator") }
    }
}
