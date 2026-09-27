package com.example.api.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

/**
 * Observability Configuration — metrics, correlation IDs, and structured request logging.
 */
@Configuration
class ObservabilityConfig(
    private val meterRegistry: MeterRegistry
) {

    @Bean
    fun logFilter(): CommonsRequestLoggingFilter {
        return CommonsRequestLoggingFilter().apply {
            setIncludeClientInfo(false)
            setIncludeQueryString(true)
            setIncludePayload(false)
            setIncludeHeaders(false)  // CRITICAL: Never log headers (Authorization, Cookie)
            setBeforeMessagePrefix("[REQUEST] ")
            setAfterMessageSuffix("")
        }
    }

    @Bean
    fun apiRequestCounter(): Counter = Counter.builder("api.requests")
        .description("Total number of API requests")
        .register(meterRegistry)

    @Bean
    fun authSuccessCounter(): Counter = Counter.builder("auth.success")
        .description("Successful authentication attempts")
        .register(meterRegistry)

    @Bean
    fun authFailureCounter(): Counter = Counter.builder("auth.failure")
        .description("Failed authentication attempts")
        .register(meterRegistry)
}
