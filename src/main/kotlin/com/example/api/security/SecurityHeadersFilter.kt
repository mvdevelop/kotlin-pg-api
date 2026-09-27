package com.example.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SecurityHeadersFilter : GenericFilterBean() {

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        filterChain: FilterChain
    ) {
        val httpResponse = response as HttpServletResponse

        httpResponse.setHeader("Content-Security-Policy",
            "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data:; font-src 'self'; connect-src 'self'; " +
                "frame-ancestors 'none'; object-src 'none'; base-uri 'self'; form-action 'self'"
        )
        httpResponse.setHeader("X-Frame-Options", "DENY")
        httpResponse.setHeader("X-Content-Type-Options", "nosniff")
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin")
        httpResponse.setHeader("X-XSS-Protection", "0")
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        httpResponse.setHeader("Pragma", "no-cache")
        httpResponse.setHeader("Expires", "0")
        httpResponse.setHeader("Permissions-Policy",
            "geolocation=(), microphone=(), camera=(), " +
                "payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=(), interest-cohort=()"
        )

        filterChain.doFilter(request, response)
    }
}
