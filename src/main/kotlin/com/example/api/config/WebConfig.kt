package com.example.api.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.*
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Web MVC Configuration — CORS, static resources, and interceptors.
 */
@Configuration
class WebConfig(
    @Value("\${CORS_ALLOWED_ORIGINS:*}") private val allowedOrigins: List<String>
) : WebMvcConfigurer {

    private val logger = LoggerFactory.getLogger(WebConfig::class.java)

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val corsConfig = CorsConfiguration().apply {
            allowedOrigins = if (allowedOrigins == listOf("*")) {
                logger.warn("CORS allowing all origins (*) — this is insecure for production!")
                listOf("*")
            } else {
                allowedOrigins
            }
            allowedHeaders = listOf(
                "Authorization", "Content-Type", "X-Requested-With", "Accept", "X-Auth-Token"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowCredentials = true
            exposedHeaders = listOf("X-RateLimit-Limit", "X-RateLimit-Remaining", "Content-Disposition")
            maxAge = 3600L
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/**", corsConfig)
        return source
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/swagger-ui/**", "/webjars/**", "/static/**")
            .addResourceLocations(
                "classpath:/static/",
                "classpath:/webjars/",
                "classpath:/swagger-ui.html"
            )
    }
}
