package com.example.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Unit tests for [RateLimitFilter].
 *
 * Tests:
 * - Normal request passes through with rate limit headers
 * - Request exceeds rate limit returns 429 with Retry-After header
 * - Rate limit is per-IP (different IPs have separate limits)
 * - Auth endpoints use stricter limits (10/min vs 100/min)
 */
@ExtendWith(SpringExtension::class)
class RateLimitFilterTest {

    private lateinit var rateLimitFilter: RateLimitFilter

    @BeforeEach
    fun setUp() {
        RateLimitConfig.clearAll()
        rateLimitFilter = RateLimitFilter()
    }

    @Test
    fun `filter should allow request under rate limit with headers`() {
        // Given
        val request = MockHttpServletRequest().apply {
            method = "GET"
            requestURI = "/api/products"
            remoteAddr = "192.168.1.1"
        }
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        // When
        rateLimitFilter.doFilter(request, response, chain)

        // Then
        verify(chain).doFilter(request, response)
        assertNotNull(response.getHeader("X-RateLimit-Limit"))
        assertNotNull(response.getHeader("X-RateLimit-Remaining"))
    }

    @Test
    fun `filter should return 429 when rate limit exceeded`() {
        // Given — exhaust the API rate limit (100 requests)
        val request = MockHttpServletRequest().apply {
            method = "GET"
            requestURI = "/api/products"
            remoteAddr = "192.168.1.2"
        }
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        // When — exhaust 100 requests + 1 extra
        repeat(100) {
            rateLimitFilter.doFilter(request, MockHttpServletResponse(), mock(FilterChain::class.java))
        }

        // The 101st request should be blocked
        rateLimitFilter.doFilter(request, response, chain)

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
        assertEquals("60", response.getHeader("Retry-After"))
    }

    @Test
    fun `auth endpoints should use stricter rate limit (10 per minute)`() {
        // Given
        val request = MockHttpServletRequest().apply {
            method = "POST"
            requestURI = "/api/auth/login"
            remoteAddr = "192.168.1.3"
        }
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        // When — exhaust 10 auth requests
        repeat(10) {
            rateLimitFilter.doFilter(request, MockHttpServletResponse(), mock(FilterChain::class.java))
        }

        // 11th request should be blocked
        rateLimitFilter.doFilter(request, response, chain)

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
        assertEquals("10", response.getHeader("X-RateLimit-Limit"))
    }

    @Test
    fun `different IPs have separate rate limit buckets`() {
        // Given
        val request1 = MockHttpServletRequest().apply {
            method = "GET"
            requestURI = "/api/products"
            remoteAddr = "192.168.1.4"
        }
        val request2 = MockHttpServletRequest().apply {
            method = "GET"
            requestURI = "/api/products"
            remoteAddr = "192.168.1.5"
        }
        val chain = mock(FilterChain::class.java)

        // When — exhaust 100 requests from IP1, then request from IP2
        repeat(100) {
            rateLimitFilter.doFilter(request1, MockHttpServletResponse(), chain)
        }

        val response2 = MockHttpServletResponse()
        rateLimitFilter.doFilter(request2, response2, chain)

        // Then — IP2 should be allowed (separate bucket)
        assertEquals(HttpStatus.OK.value(), response2.status)
    }

    @Test
    fun `filter should handle X-Forwarded-For header`() {
        // Given
        val request = MockHttpServletRequest().apply {
            method = "GET"
            requestURI = "/api/products"
            addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.100")
            remoteAddr = "127.0.0.1"
        }
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        // When
        rateLimitFilter.doFilter(request, response, chain)

        // Then
        verify(chain).doFilter(request, response)
        // The IP should be extracted from X-Forwarded-For
    }
}
