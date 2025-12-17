
package com.exemplo.api.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal  // Adicionar import

data class ProdutoDTO(
    @field:NotBlank(message = "O nome é obrigatório")
    @field:Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    val nome: String,
    
    @field:NotBlank(message = "A descrição é obrigatória")
    @field:Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres")
    val descricao: String,
    
    @field:NotNull(message = "O preço é obrigatório")
    @field:DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    val preco: BigDecimal,
    
    @field:NotNull(message = "A quantidade é obrigatória")
    @field:DecimalMin(value = "0.0", message = "A quantidade não pode ser negativa")
    val quantidade: Int
)
