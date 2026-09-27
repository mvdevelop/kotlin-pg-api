package com.example.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.springframework.mock.web.MockHttpServletResponse

import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Unit tests for [SecurityHeadersFilter].
 *
 * Verifies that all required security headers are set on responses.
 *
 * OWASP ASVS 4.0.3 — V1.10: Security headers verification
 */
@ExtendWith(SpringExtension::class)
class SecurityHeadersFilterTest {

    private val filter = SecurityHeadersFilter()

    @Test
    fun `filter should set all required security headers`() {
        // Given
        val request = mock(HttpServletRequest::class.java)
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        // When
        filter.doFilter(request, response, chain)

        // Then — verify all required security headers are present
        assert(response.getHeader("Content-Security-Policy") != null) {
            "Content-Security-Policy header must be present"
        }
        assert(response.getHeader("Content-Security-Policy")?.contains("default-src 'self'") == true) {
            "CSP must include default-src 'self'"
        }
        assert(response.getHeader("X-Frame-Options") == "DENY") {
            "X-Frame-Options must be DENY"
        }
        assert(response.getHeader("X-Content-Type-Options") == "nosniff") {
            "X-Content-Type-Options must be nosniff"
        }
        assert(response.getHeader("Strict-Transport-Security")?.contains("max-age=31536000") == true) {
            "HSTS must be set with 1-year max-age"
        }
        assert(response.getHeader("Referrer-Policy") == "strict-origin-when-cross-origin") {
            "Referrer-Policy must be strict-origin-when-cross-origin"
        }
        assert(response.getHeader("Cache-Control")?.contains("no-store") == true) {
            "Cache-Control must prevent caching"
        }
        assert(response.getHeader("X-XSS-Protection") == "0") {
            "X-XSS-Protection must be 0 (rely on CSP)"
        }

        verify(chain).doFilter(request, response)
    }

    @Test
    fun `filter should set Permissions-Policy header`() {
        // Given
        val request = mock(HttpServletRequest::class.java)
        val response = MockHttpServletResponse()
        val chain = mock(FilterChain::class.java)

        // When
        filter.doFilter(request, response, chain)

        // Then
        val permissionsPolicy = response.getHeader("Permissions-Policy")
        assertNotNull(permissionsPolicy)
        assertTrue(permissionsPolicy.contains("camera=()") || permissionsPolicy.contains("camera="))
        assertTrue(permissionsPolicy.contains("geolocation=()") || permissionsPolicy.contains("geolocation="))
    }
}
