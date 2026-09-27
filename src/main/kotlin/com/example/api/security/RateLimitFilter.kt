package com.example.api.security

import com.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
class RateLimitFilter : GenericFilterBean() {

    private val logger = LoggerFactory.getLogger(RateLimitFilter::class.java)

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        filterChain: FilterChain
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        val ipAddress = getClientIp(httpRequest)
        val requestPath = httpRequest.requestURI

        val isAuthEndpoint = requestPath.startsWith("/api/auth")
        val bucket = if (isAuthEndpoint) {
            RateLimitConfig.getAuthBucket(ipAddress)
        } else {
            RateLimitConfig.getApiBucket(ipAddress)
        }

        val consumption = bucket.tryConsumeAndReturnRemaining(1)

        if (consumption != null) {
            val limit = if (isAuthEndpoint) RateLimitConfig.AUTH_RATE_LIMIT else RateLimitConfig.API_RATE_LIMIT
            httpResponse.setHeader("X-RateLimit-Limit", limit.toString())
            httpResponse.setHeader("X-RateLimit-Remaining", consumption.remaining.toString())
            httpResponse.setHeader("X-RateLimit-Reset",
                (System.currentTimeMillis() + consumption.ttlAfterWaiting.inWholeMilliseconds).toString())
            filterChain.doFilter(request, response)
        } else {
            logger.warn("Rate limit exceeded for IP: {} on path: {}", ipAddress, requestPath)
            httpResponse.status = HttpStatus.TOO_MANY_REQUESTS.value()
            httpResponse.setHeader("Retry-After", "60")
            httpResponse.setHeader("Content-Type", "application/json")
            httpResponse.writer.write(
                "{\"error\":\"rate_limit_exceeded\",\"message\":\"Too many requests. Please try again later.\"}"
            )
        }
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        if (forwarded != null && forwarded.isNotEmpty()) {
            return forwarded.split(",")[0].trim()
        }
        return request.remoteAddr ?: "unknown"
    }
}
