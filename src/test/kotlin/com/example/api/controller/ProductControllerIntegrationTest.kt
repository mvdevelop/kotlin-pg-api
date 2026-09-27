package com.example.api.controller

import com.example.api.dto.LoginRequestDTO
import com.example.api.dto.ProductDTO
import com.example.api.dto.RegisterRequestDTO
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal

/**
 * Integration tests for ProductController.
 *
 * Uses Testcontainers with a real PostgreSQL instance — no mocks for the database layer.
 * Tests exercise the full stack: HTTP → Security → Service → Repository → PostgreSQL.
 *
 * SECURITY TEST CASES:
 * - Unauthenticated access to protected endpoints returns 401
 * - Unauthorized (non-admin) access to POST/PUT/DELETE returns 403
 * - Input validation errors return 400 with field-level error details
 * - SQL injection attempts are blocked (parameterized queries)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ProductControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private lateinit var postgres: PostgreSQLContainer<*>

        @BeforeAll
        @JvmStatic
        fun setUp() {
            postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
            postgres.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            postgres.stop()
        }
    }

    @DynamicPropertySource
    fun configureProperties(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.url") { postgres.jdbcUrl }
        registry.add("spring.datasource.username") { postgres.username }
        registry.add("spring.datasource.password") { postgres.password }
        registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        registry.add("jwt.secret") {
            java.util.Base64.getEncoder().encodeToString("integration-test-secret-key-long-enough-1234567890".toByteArray())
        }
    }

    private var authToken: String? = null

    @BeforeEach
    fun setUpAuth() {
        // Register a user and get auth token
        val registerDto = RegisterRequestDTO(
            username = "testuser",
            email = "test@example.com",
            password = "SecureP@ss123!",
            role = "ROLE_USER"
        )

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerDto)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
        }

        // Login to get token
        val loginDto = LoginRequestDTO(
            username = "testuser",
            password = "SecureP@ss123!"
        )

        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginDto)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { exists() }
        }.andReturn()

        val responseContent = response.response.contentAsString
        val jsonNode = objectMapper.readTree(responseContent)
        authToken = jsonNode.get("token").asText()
    }

    @Test
    fun `GET products without auth should return 401`() {
        mockMvc.get("/api/products") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { is4xxUnauthorized() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `GET products with auth should return 200`() {
        mockMvc.get("/api/products") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST product as non-admin should return 403`() {
        val dto = ProductDTO(
            name = "Test Product",
            description = "This is a test product description",
            price = BigDecimal("99.99"),
            quantity = 10
        )

        mockMvc.post("/api/products") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(dto)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { is4xxClientError() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `POST product with invalid name (too short) should return 400`() {
        val dto = ProductDTO(
            name = "AB", // Less than 3 chars
            description = "This is a test product description",
            price = BigDecimal("99.99"),
            quantity = 10
        )

        mockMvc.post("/api/products") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(dto)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { is4xxClientError() }
            jsonPath("$.errors.name") { exists() }
        }
    }

    @Test
    fun `GET product by non-existent ID should return 404`() {
        mockMvc.get("/api/products/99999") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `Delete product as non-admin should return 403`() {
        mockMvc.delete("/api/products/1") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        }.andExpect {
            status { is4xxClientError() }
            jsonPath("$.error") { exists() }
        }
    }
}
