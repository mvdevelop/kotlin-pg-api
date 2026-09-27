package com.example.api.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

/**
 * Unit tests for [JwtTokenProvider].
 *
 * Tests token generation, validation, and parsing.
 * Uses a real JWT secret key for accurate testing.
 */
class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        // Use a valid Base64-encoded 256-bit (32-byte) secret key
        val secretKey = Base64.getEncoder().encodeToString("test-secret-key-for-testing-123456".toByteArray())
        jwtTokenProvider = JwtTokenProvider(secretKey, 3600000) // 1 hour expiration
    }

    @Test
    fun `createToken should generate valid JWT token`() {
        // Given
        val userDetails = User.builder()
            .username("testuser")
            .password("hashed-password")
            .authorities(SimpleGrantedAuthority("ROLE_USER"))
            .build()
        val authentication = org.springframework.security.authentication.TestingAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )

        // When
        val token = jwtTokenProvider.createToken(authentication)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(jwtTokenProvider.validateToken(token))
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token))
    }

    @Test
    fun `validateToken should return false for expired token`() {
        // Given — token with negative expiration (already expired)
        val secretKey = Base64.getEncoder().encodeToString("test-secret-key-for-testing-123456".toByteArray())
        val expiredProvider = JwtTokenProvider(secretKey, -1000) // Expires 1 second ago

        val userDetails = User.builder()
            .username("testuser")
            .password("hashed-password")
            .authorities(SimpleGrantedAuthority("ROLE_USER"))
            .build()
        val authentication = org.springframework.security.authentication.TestingAuthenticationToken(
            userDetails, null, userDetails.authorities
        )

        val token = expiredProvider.createToken(authentication)

        // Small delay to ensure expiration
        Thread.sleep(2000)

        // Then
        assertFalse(expiredProvider.validateToken(token))
    }

    @Test
    fun `validateToken should return false for tampered token`() {
        // Given
        val userDetails = User.builder()
            .username("testuser")
            .password("hashed-password")
            .authorities(SimpleGrantedAuthority("ROLE_USER"))
            .build()
        val authentication = org.springframework.security.authentication.TestingAuthenticationToken(
            userDetails, null, userDetails.authorities
        )
        val token = jwtTokenProvider.createToken(authentication)

        // Tamper with the token — change a character in the payload
        val parts = token.split(".")
        val tamperedPayload = parts[1].dropLast(1) + if (parts[1].last() == 'A') 'B' else 'A'
        val tamperedToken = "${parts[0]}.$tamperedPayload.${parts[2]}"

        // Then
        assertFalse(jwtTokenProvider.validateToken(tamperedToken))
    }

    @Test
    fun `resolveToken should extract token from Bearer header`() {
        // Given
        val token = "eyJhbGciOiJIUzI1NiJ9.valid-token.signature"
        val bearerHeader = "Bearer $token"

        // When
        val resolved = JwtTokenProvider.resolveToken(bearerHeader)

        // Then
        assertEquals(token, resolved)
    }

    @Test
    fun `resolveToken should return null for null header`() {
        // Given
        val header: String? = null

        // When
        val resolved = JwtTokenProvider.resolveToken(header)

        // Then
        assertNull(resolved)
    }

    @Test
    fun `resolveToken should return null for invalid scheme`() {
        // Given
        val header = "Basic dXNlcjpwYXNzd29yZA=="

        // When
        val resolved = JwtTokenProvider.resolveToken(header)

        // Then
        assertNull(resolved)
    }

    @Test
    fun `resolveToken should return null for empty Bearer`() {
        // Given
        val header = "Bearer "

        // When
        val resolved = JwtTokenProvider.resolveToken(header)

        // Then
        assertNull(resolved)
    }
}
