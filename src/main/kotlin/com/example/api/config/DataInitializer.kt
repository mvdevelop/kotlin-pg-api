package com.example.api.config

import com.example.api.model.User
import com.example.api.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Data Initializer — seeds an admin user for development.
 * Only runs in `dev` profile — never in production.
 */
@Configuration
@Profile("dev")
class DataInitializer {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @Bean
    fun dataInitializer(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): CommandLineRunner {
        return CommandLineRunner {
            val adminUsername = System.getenv("ADMIN_USERNAME") ?: "admin"
            val adminPassword = System.getenv("ADMIN_PASSWORD") ?: "AdminP@ssw0rd!23"

            if (userRepository.findByUsername(adminUsername) == null) {
                val adminUser = User(
                    username = adminUsername,
                    passwordHash = passwordEncoder.encode(adminPassword),
                    email = "admin@example.com",
                    roles = setOf("ROLE_ADMIN")
                )
                userRepository.save(adminUser)
                logger.info("Created admin user: {} (ROLE_ADMIN)", adminUsername)
            }
        }
    }
}
