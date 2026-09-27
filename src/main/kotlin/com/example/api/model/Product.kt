package com.example.api.model

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Product JPA entity representing the `products` table.
 */
@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_products_name", columnList = "name"),
        Index(name = "idx_products_price", columnList = "price")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_products_name", columnNames = ["name"])
    ]
)
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @field:NotBlank(message = "Description is required")
    @field:Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    @Column(name = "description", nullable = false, length = 500)
    var description: String,

    @field:NotBlank(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @field:NotBlank(message = "Quantity is required")
    @field:DecimalMin(value = "0", message = "Quantity cannot be negative")
    @Column(name = "quantity", nullable = false)
    var quantity: Int,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
