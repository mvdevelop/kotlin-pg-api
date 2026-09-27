package com.example.api.service

import com.example.api.dto.LoginRequestDTO
import com.example.api.dto.RegisterRequestDTO
import com.example.api.model.User
import com.example.api.repository.UserRepository
import com.example.api.security.JwtTokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

/**
 * Unit tests for [AuthService].
 *
 * Tests:
 * - Successful user registration
 * - Registration with duplicate username (throws)
 * - Successful login (returns JWT)
 * - Failed login (BadCredentialsException)
 *
 * Uses MockK — no database required.
 */
class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: BCryptPasswordEncoder
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        authenticationManager = mockk()
        jwtTokenProvider = mockk()

        authService = AuthService(
            userRepository,
            passwordEncoder,
            authenticationManager,
            jwtTokenProvider
        )
    }

    @Test
    fun `register should create user when username is available`() {
        // Given
        val request = RegisterRequestDTO(
            username = "newuser",
            email = "user@example.com",
            password = "SecureP@ssw0rd!23",
            role = "ROLE_USER"
        )
        val savedUser = User(
            id = 1L,
            username = request.username,
            passwordHash = "hashed-password",
            email = request.email,
            roles = setOf("ROLE_USER")
        )

        every { userRepository.existsByUsername(request.username) } returns false
        every { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns "hashed-password"
        every { userRepository.save(any()) } returns savedUser

        // When
        val result = authService.register(request)

        // Then
        assertNotNull(result)
        assertEquals("newuser", result.username)
        assertEquals("hashed-password", result.passwordHash)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `register should throw when username already exists`() {
        // Given
        val request = RegisterRequestDTO(
            username = "existinguser",
            email = "user@example.com",
            password = "SecureP@ssw0rd!23",
            role = "ROLE_USER"
        )

        every { userRepository.existsByUsername("existinguser") } returns true
        every { userRepository.existsByEmail(any()) } returns false

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            authService.register(request)
        }
        assertTrue(exception.message?.contains("Registration failed") == true)
    }

    @Test
    fun `register should throw when email already exists`() {
        // Given
        val request = RegisterRequestDTO(
            username = "newuser",
            email = "existing@example.com",
            password = "SecureP@ssw0rd!23",
            role = "ROLE_USER"
        )

        every { userRepository.existsByUsername(any()) } returns false
        every { userRepository.existsByEmail("existing@example.com") } returns true

        // When & Then
        assertThrows<IllegalArgumentException> {
            authService.register(request)
        }
    }

    @Test
    fun `login should return JWT token when credentials valid`() {
        // Given
        val request = LoginRequestDTO(
            username = "testuser",
            password = "SecureP@ssw0rd!23"
        )

        val userDetails = org.springframework.security.core.userdetails.User.builder()
            .username("testuser")
            .password("hashed-password")
            .authorities("ROLE_USER")
            .build()

        val authentication = TestingAuthenticationToken(
            userDetails, null, userDetails.authorities
        )

        every { authenticationManager.authenticate(any()) } returns authentication
        every { jwtTokenProvider.createToken(authentication) } returns "valid-jwt-token"

        // When
        val result = authService.login(request)

        // Then
        assertEquals("valid-jwt-token", result.token)
        assertTrue(result.expiresIn > 0)
    }

    @Test
    fun `login should throw BadCredentialsException when credentials invalid`() {
        // Given
        val request = LoginRequestDTO(
            username = "testuser",
            password = "wrong-password"
        )

        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Bad credentials")

        // When & Then
        assertThrows<BadCredentialsException> {
            authService.login(request)
        }
    }
}
