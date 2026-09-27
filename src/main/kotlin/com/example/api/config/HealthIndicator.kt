package com.example.api.config

import com.example.api.repository.ProductRepository
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * Custom health indicator for application-level checks.
 */
@Component
class AppHealthIndicator(
    private val productRepository: ProductRepository
) : HealthIndicator {

    override fun health(): Health {
        return try {
            productRepository.count()
            val jwtSecret = System.getenv("JWT_SECRET") ?: ""
            if (jwtSecret.isBlank() || jwtSecret.length < 32) {
                return Health.down()
                    .withDetail("reason", "JWT_SECRET not configured or too short")
                    .build()
            }
            Health.up()
                .withDetail("status", "All checks passed")
                .build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", "Database connectivity check failed")
                .build()
        }
    }
}
