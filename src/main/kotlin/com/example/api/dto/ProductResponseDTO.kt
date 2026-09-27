package com.example.api.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductResponseDTO(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val quantity: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
