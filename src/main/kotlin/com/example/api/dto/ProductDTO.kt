package com.example.api.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ProductDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String,

    @field:NotBlank(message = "Description is required")
    @field:Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    val description: String,

    @field:NotBlank(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than zero")
    val price: BigDecimal,

    @field:NotBlank(message = "Quantity is required")
    @field:DecimalMin(value = "0", message = "Quantity cannot be negative")
    val quantity: Int
)
