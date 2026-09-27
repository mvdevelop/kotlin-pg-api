package com.example.api.service

import com.example.api.dto.ProductDTO
import com.example.api.dto.ProductResponseDTO
import com.example.api.exception.ResourceNotFoundException
import com.example.api.model.Product
import com.example.api.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository
) {

    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    @Transactional
    fun createProduct(dto: ProductDTO): ProductResponseDTO {
        logger.info("Creating new product with name: {}", "***")
        val product = Product(
            name = dto.name,
            description = dto.description,
            price = dto.price,
            quantity = dto.quantity
        )
        val savedProduct = productRepository.save(product)
        logger.info("Product created: id={}", savedProduct.id)
        return savedProduct.toResponseDTO()
    }

    fun listAllProducts(): List<ProductResponseDTO> {
        return productRepository.findAll().map { it.toResponseDTO() }
    }

    fun listProductsPaginated(pageable: Pageable): Page<ProductResponseDTO> {
        return productRepository.findAll(pageable).map { it.toResponseDTO() }
    }

    fun getProductById(id: Long): ProductResponseDTO {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with ID: $id") }
        return product.toResponseDTO()
    }

    @Transactional
    fun updateProduct(id: Long, dto: ProductDTO): ProductResponseDTO {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with ID: $id") }
        product.name = dto.name
        product.description = dto.description
        product.price = dto.price
        product.quantity = dto.quantity
        val updatedProduct = productRepository.save(product)
        logger.info("Product updated: id={}", updatedProduct.id)
        return updatedProduct.toResponseDTO()
    }

    @Transactional
    fun deleteProduct(id: Long) {
        if (!productRepository.existsById(id)) {
            throw ResourceNotFoundException("Product not found with ID: $id")
        }
        productRepository.deleteById(id)
        logger.info("Product deleted: id={}", id)
    }

    fun searchProductsByName(name: String): List<ProductResponseDTO> {
        return productRepository.findByNameContainingIgnoreCase(name).map { it.toResponseDTO() }
    }

    fun findByPriceRange(minPrice: BigDecimal, maxPrice: BigDecimal): List<ProductResponseDTO> {
        return productRepository.findByPriceRange(minPrice, maxPrice).map { it.toResponseDTO() }
    }

    fun getMostExpensiveProduct(): ProductResponseDTO? {
        return productRepository.findFirstByOrderByPriceDesc()?.toResponseDTO()
    }

    fun calculateAveragePrice(): BigDecimal? {
        return productRepository.findAveragePrice()
    }

    private fun Product.toResponseDTO(): ProductResponseDTO {
        return ProductResponseDTO(
            id = this.id!!,
            name = this.name,
            description = this.description,
            price = this.price,
            quantity = this.quantity,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
