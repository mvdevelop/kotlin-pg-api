package com.example.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main entry point for the Kotlin Product API.
 *
 * This is a secure, production-ready REST API built with:
 * - Spring Boot 3.5.x (LTS)
 * - Kotlin 2.1.x
 * - PostgreSQL 16+
 * - JWT authentication (Spring Security 6)
 * - OWASP ASVS Level 2 aligned security controls
 */
@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
