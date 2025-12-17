
package com.exemplo.api.repository

import com.exemplo.api.model.Produto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal  // Adicionar este import
import java.util.Optional

@Repository
interface ProdutoRepository : JpaRepository<Produto, Long> {
    
    fun findByNomeContainingIgnoreCase(nome: String): List<Produto>
    
    @Query("SELECT p FROM Produto p WHERE p.preco BETWEEN :minPrice AND :maxPrice")
    fun findByPriceRange(
        @Param("minPrice") minPrice: BigDecimal, 
        @Param("maxPrice") maxPrice: BigDecimal
    ): List<Produto>
    
    fun findByQuantidadeGreaterThan(quantidade: Int): List<Produto>
    
    fun findFirstByOrderByPrecoDesc(): Optional<Produto>
    
    @Query("SELECT AVG(p.preco) FROM Produto p")
    fun findAveragePrice(): BigDecimal?
}
