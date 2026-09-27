package com.example.api.controller

import com.example.api.dto.LoginRequestDTO
import com.example.api.dto.RegisterRequestDTO
import com.example.api.service.AuthService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Unit tests for [AuthController].
 *
 * Uses @WebMvcTest — only the web layer is loaded (no DB, no Security filter chain).
 * The AuthService is mocked with MockBean.
 *
 * Tests cover:
 * - Registration success
 * - Registration with duplicate username (409)
 * - Login success (returns JWT token)
 * - Login with invalid credentials (401)
 * - Input validation errors (400)
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(AuthController::class)
class AuthControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var authService: AuthService

    @Test
    fun `register should return 201 on successful registration`() {
        // Given
        val request = RegisterRequestDTO(
            username = "newuser",
            email = "user@example.com",
            password = "SecureP@ssw0rd!23",
            role = "ROLE_USER"
        )

        `when`(authService.register(request)).thenReturn(
            com.example.api.model.User(
                id = 1L,
                username = request.username,
                passwordHash = "hash",
                email = request.email,
                roles = setOf("ROLE_USER")
            )
        )

        // When & Then
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            jsonPath("$.message") { value("User registered successfully") }
        }
    }

    @Test
    fun `register should return 409 when username already exists`() {
        // Given
        val request = RegisterRequestDTO(
            username = "existing",
            email = "user@example.com",
            password = "SecureP@ssw0rd!23",
            role = "ROLE_USER"
        )

        `when`(authService.register(request)).thenThrow(
            IllegalArgumentException("Registration failed — username or email already in use")
        )

        // When & Then
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("conflict") }
        }
    }

    @Test
    fun `register should return 400 for invalid input (short password)`() {
        // Given
        val request = RegisterRequestDTO(
            username = "valid",
            email = "user@example.com",
            password = "short", // Violates 12+ chars requirement
            role = "ROLE_USER"
        )

        // When & Then
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
        }
    }

    @Test
    fun `login should return 200 and JWT token on success`() {
        // Given
        val request = LoginRequestDTO(
            username = "testuser",
            password = "SecureP@ssw0rd!23"
        )

        `when`(authService.login(request)).thenReturn(
            com.example.api.dto.LoginResponseDTO(
                token = "eyJhbGciOiJIUzI1NiJ9.valid.jwt.token",
                expiresIn = 86400L
            )
        )

        // When & Then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { exists() }
            jsonPath("$.expiresIn") { value(86400) }
            jsonPath("$.tokenType") { value("Bearer") }
        }
    }

    @Test
    fun `login should return 401 for invalid credentials`() {
        // Given
        val request = LoginRequestDTO(
            username = "testuser",
            password = "wrong-password"
        )

        `when`(authService.login(request)).thenThrow(
            BadCredentialsException("Bad credentials")
        )

        // When & Then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.error") { value("invalid_credentials") }
            jsonPath("$.message") { value("Invalid username or password") }
        }
    }
}
