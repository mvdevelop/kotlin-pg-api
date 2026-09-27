package com.example.api.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Request Logging & Correlation ID Filter.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class RequestLoggingFilter : Filter {

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        val httpRequest = request as HttpServletRequest
        val correlationId = httpRequest.getHeader("X-Correlation-Id")
            ?: UUID.randomUUID().toString()

        MDC.put("correlationId", correlationId)

        try {
            chain.doFilter(request, response)
        } finally {
            MDC.remove("correlationId")
        }
    }
}
