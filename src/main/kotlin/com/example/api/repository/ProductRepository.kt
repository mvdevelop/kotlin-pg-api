package com.example.api.repository

import com.example.api.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    fun findByNameContainingIgnoreCase(name: String): List<Product>

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    fun findByPriceRange(
        @Param("minPrice") minPrice: BigDecimal,
        @Param("maxPrice") maxPrice: BigDecimal
    ): List<Product>

    fun findByQuantityGreaterThan(quantity: Int): List<Product>

    fun findFirstByOrderByPriceDesc(): Product?

    @Query("SELECT AVG(p.price) FROM Product p")
    fun findAveragePrice(): BigDecimal?
}
