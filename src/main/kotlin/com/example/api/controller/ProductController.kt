package com.example.api.controller

import com.example.api.dto.ProductDTO
import com.example.api.dto.ProductResponseDTO
import com.example.api.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints — requires authentication")
@Validated
class ProductController(
    private val productService: ProductService
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", description = "Creates a new product in the catalog. Requires ADMIN role.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Product created successfully"),
        ApiResponse(responseCode = "400", description = "Validation error — invalid input data"),
        ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required")
    )
    fun createProduct(@Valid @RequestBody dto: ProductDTO): ResponseEntity<ProductResponseDTO> {
        val product = productService.createProduct(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Returns a list of all products in the catalog.")
    fun listAllProducts(): ResponseEntity<List<ProductResponseDTO>> {
        val products = productService.listAllProducts()
        return ResponseEntity.ok(products)
    }

    @GetMapping("/paginated")
    @Operation(summary = "List products (paginated)", description = "Returns a paginated list of products.")
    fun listProductsPaginated(
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<ProductResponseDTO>> {
        val products = productService.listProductsPaginated(pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its unique identifier.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Product found"),
        ApiResponse(responseCode = "404", description = "Product not found")
    )
    fun getProductById(
        @Parameter(description = "Unique product ID", example = "1", in = ParameterIn.PATH)
        @PathVariable id: Long
    ): ResponseEntity<ProductResponseDTO> {
        val product = productService.getProductById(id)
        return ResponseEntity.ok(product)
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name", description = "Performs a case-insensitive partial match search on product names.")
    fun searchProducts(
        @Parameter(description = "Search term (minimum 2 characters)", example = "notebook")
        @RequestParam name: String
    ): ResponseEntity<List<ProductResponseDTO>> {
        val products = productService.searchProductsByName(name)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/price-range")
    @Operation(summary = "Filter products by price range", description = "Returns products within the specified minimum and maximum price.")
    fun findByPriceRange(
        @Parameter(description = "Minimum price (inclusive)", example = "100.00")
        @RequestParam minPrice: BigDecimal,
        @Parameter(description = "Maximum price (inclusive)", example = "5000.00")
        @RequestParam maxPrice: BigDecimal
    ): ResponseEntity<List<ProductResponseDTO>> {
        val products = productService.findByPriceRange(minPrice, maxPrice)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/most-expensive")
    @Operation(summary = "Get most expensive product", description = "Returns the product with the highest price.")
    fun getMostExpensiveProduct(): ResponseEntity<ProductResponseDTO?> {
        val product = productService.getMostExpensiveProduct()
        return ResponseEntity.ok(product)
    }

    @GetMapping("/average-price")
    @Operation(summary = "Calculate average product price", description = "Returns the average price across all products.")
    fun calculateAveragePrice(): ResponseEntity<BigDecimal?> {
        val average = productService.calculateAveragePrice()
        return ResponseEntity.ok(average)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product", description = "Updates an existing product. Requires ADMIN role.")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody dto: ProductDTO
    ): ResponseEntity<ProductResponseDTO> {
        val product = productService.updateProduct(id, dto)
        return ResponseEntity.ok(product)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Removes a product permanently. Requires ADMIN role.")
    fun deleteProduct(
        @Parameter(description = "Unique product ID")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }
}
