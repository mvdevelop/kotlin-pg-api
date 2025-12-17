
package com.exemplo.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${api.version}") private val apiVersion: String,
    @Value("\${api.title}") private val apiTitle: String
) {

    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI().info(
            Info()
                .title(apiTitle)
                .version(apiVersion)
                .description("API REST desenvolvida em Kotlin com Spring Boot 3 e PostgreSQL")
                .contact(
                    Contact()
                        .name("Sua Empresa")
                        .email("contato@empresa.com")
                        .url("https://www.empresa.com")
                )
                .license(
                    License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                )
                .termsOfService("https://www.empresa.com/terms")
        )
}
