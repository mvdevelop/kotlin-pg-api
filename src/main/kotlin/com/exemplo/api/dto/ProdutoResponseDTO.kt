
package com.exemplo.api.dto

import java.math.BigDecimal  // Adicionar import
import java.time.LocalDateTime

data class ProdutoResponseDTO(
    val id: Long,
    val nome: String,
    val descricao: String,
    val preco: BigDecimal,
    val quantidade: Int,
    val dataCriacao: LocalDateTime,
    val dataAtualizacao: LocalDateTime
)
