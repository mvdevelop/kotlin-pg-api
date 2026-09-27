package com.example.api.security

import com.github.bucket4j.Bandwidth
import com.github.bucket4j.Bucket
import com.github.bucket4j.Bucket4j
import com.github.bucket4j.Refill
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

object RateLimitConfig {

    private val logger = LoggerFactory.getLogger(RateLimitConfig::class.java)

    const val AUTH_RATE_LIMIT = 10L
    const val API_RATE_LIMIT = 100L
    val RATE_LIMIT_WINDOW: Duration = Duration.ofMinutes(1)

    private val buckets: ConcurrentHashMap<String, Bucket> = ConcurrentHashMap()

    fun getBucket(key: String, capacity: Long): Bucket {
        return buckets.computeIfAbsent(key) {
            Bucket4j.builder()
                .addLimit(
                    Bandwidth.classic(capacity, Refill.greedy(capacity, RATE_LIMIT_WINDOW))
                        .withWeight(1)
                )
                .build()
        }
    }

    fun getAuthBucket(ipAddress: String): Bucket = getBucket("auth:$ipAddress", AUTH_RATE_LIMIT)
    fun getApiBucket(ipAddress: String): Bucket = getBucket("api:$ipAddress", API_RATE_LIMIT)

    fun clearAll() {
        buckets.clear()
    }
}
