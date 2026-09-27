package com.example.api.service

import com.example.api.dto.ProductDTO
import com.example.api.exception.ResourceNotFoundException
import com.example.api.model.Product
import com.example.api.repository.ProductRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Unit tests for [ProductService].
 *
 * Uses MockK for mocking the repository layer — no database required.
 * Coverage targets:
 * - Happy path for all CRUD operations
 * - Edge cases: not found, validation failures
 * - Business logic: price range, most expensive, average
 */
@ExtendWith(SpringExtension::class)
class ProductServiceTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productRepository = mockk()
        productService = ProductService(productRepository)
    }

    @Test
    fun `createProduct should return ProductResponseDTO when valid input`() {
        // Given
        val dto = ProductDTO(
            name = "Test Product",
            description = "This is a test product description",
            price = BigDecimal("99.99"),
            quantity = 10
        )
        val savedProduct = Product(
            id = 1L,
            name = dto.name,
            description = dto.description,
            price = dto.price,
            quantity = dto.quantity,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { productRepository.save(any()) } returns savedProduct

        // When
        val result = productService.createProduct(dto)

        // Then
        assertEquals(1L, result.id)
        assertEquals("Test Product", result.name)
        assertEquals(BigDecimal("99.99"), result.price)
        verify(exactly = 1) { productRepository.save(any()) }
    }

    @Test
    fun `getProductById should throw ResourceNotFoundException when product not found`() {
        // Given
        val productId = 999L
        every { productRepository.findById(productId) } returns java.util.Optional.empty()

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            productService.getProductById(productId)
        }
        assertTrue(exception.message!!.contains("Product not found with ID: $productId"))
    }

    @Test
    fun `getProductById should return ProductResponseDTO when product exists`() {
        // Given
        val product = Product(
            id = 1L,
            name = "Test Product",
            description = "Test description with length",
            price = BigDecimal("99.99"),
            quantity = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        every { productRepository.findById(1L) } returns java.util.Optional.of(product)

        // When
        val result = productService.getProductById(1L)

        // Then
        assertEquals(1L, result.id)
        assertEquals("Test Product", result.name)
    }

    @Test
    fun `updateProduct should throw ResourceNotFoundException when product not found`() {
        // Given
        val dto = ProductDTO(
            name = "Updated Product",
            description = "Updated description with length",
            price = BigDecimal("149.99"),
            quantity = 20
        )
        every { productRepository.findById(1L) } returns java.util.Optional.empty()

        // When & Then
        assertThrows(ResourceNotFoundException::class.java) {
            productService.updateProduct(1L, dto)
        }
    }

    @Test
    fun `updateProduct should return updated ProductResponseDTO when product exists`() {
        // Given
        val existingProduct = Product(
            id = 1L,
            name = "Old Product",
            description = "Old description with length",
            price = BigDecimal("99.99"),
            quantity = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val dto = ProductDTO(
            name = "Updated Product",
            description = "Updated description with length",
            price = BigDecimal("149.99"),
            quantity = 20
        )
        val updatedProduct = existingProduct.copy(
            name = dto.name,
            description = dto.description,
            price = dto.price,
            quantity = dto.quantity,
            updatedAt = LocalDateTime.now()
        )

        every { productRepository.findById(1L) } returns java.util.Optional.of(existingProduct)
        every { productRepository.save(any()) } returns updatedProduct

        // When
        val result = productService.updateProduct(1L, dto)

        // Then
        assertEquals("Updated Product", result.name)
        assertEquals(BigDecimal("149.99"), result.price)
        assertEquals(20, result.quantity)
    }

    @Test
    fun `deleteProduct should throw ResourceNotFoundException when product not found`() {
        // Given
        every { productRepository.existsById(1L) } returns false

        // When & Then
        assertThrows(ResourceNotFoundException::class.java) {
            productService.deleteProduct(1L)
        }
    }

    @Test
    fun `deleteProduct should delete product when it exists`() {
        // Given
        every { productRepository.existsById(1L) } returns true
        every { productRepository.deleteById(1L) } just Runs

        // When
        productService.deleteProduct(1L)

        // Then
        verify { productRepository.deleteById(1L) }
    }

    @Test
    fun `listAllProducts should return list of ProductResponseDTO`() {
        // Given
        val products = listOf(
            Product(1L, "Product 1", "Description 1 longer text", BigDecimal("10.00"), 5, LocalDateTime.now(), LocalDateTime.now()),
            Product(2L, "Product 2", "Description 2 longer text", BigDecimal("20.00"), 15, LocalDateTime.now(), LocalDateTime.now())
        )
        every { productRepository.findAll() } returns products

        // When
        val result = productService.listAllProducts()

        // Then
        assertEquals(2, result.size)
        assertEquals("Product 1", result[0].name)
        assertEquals("Product 2", result[1].name)
    }

    @Test
    fun `listProductsPaginated should return paginated ProductResponseDTO`() {
        // Given
        val product = Product(
            id = 1L,
            name = "Product 1",
            description = "Description 1 longer text",
            price = BigDecimal("10.00"),
            quantity = 5,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val page = PageImpl(listOf(product))
        val pageable = PageRequest.of(0, 10)

        every { productRepository.findAll(pageable) } returns page

        // When
        val result = productService.listProductsPaginated(pageable)

        // Then
        assertTrue(result is Page<*>)
        assertEquals(1, result.totalElements)
    }

    @Test
    fun `findByPriceRange should return products within range`() {
        // Given
        val products = listOf(
            Product(1L, "Product 1", "Description 1 longer text", BigDecimal("50.00"), 5, LocalDateTime.now(), LocalDateTime.now())
        )
        every { productRepository.findByPriceRange(any(), any()) } returns products

        // When
        val result = productService.findByPriceRange(BigDecimal("10.00"), BigDecimal("100.00"))

        // Then
        assertEquals(1, result.size)
        assertEquals(BigDecimal("50.00"), result[0].price)
    }

    @Test
    fun `getMostExpensiveProduct should return the product with highest price`() {
        // Given
        val product = Product(
            id = 1L,
            name = "Most Expensive",
            description = "Description with minimum length",
            price = BigDecimal("9999.99"),
            quantity = 5,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        every { productRepository.findFirstByOrderByPriceDesc() } returns product

        // When
        val result = productService.getMostExpensiveProduct()

        // Then
        assertNotNull(result)
        assertEquals("Most Expensive", result!!.name)
    }

    @Test
    fun `calculateAveragePrice should return the average price`() {
        // Given
        every { productRepository.findAveragePrice() } returns BigDecimal("150.00")

        // When
        val result = productService.calculateAveragePrice()

        // Then
        assertEquals(BigDecimal("150.00"), result)
    }
}
