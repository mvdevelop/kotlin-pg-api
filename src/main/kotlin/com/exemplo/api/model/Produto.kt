
package com.exemplo.api.model

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "produtos")
data class Produto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false)
    var nome: String,
    
    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres")
    @Column(nullable = false)
    var descricao: String,
    
    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    var preco: BigDecimal,
    
    @NotNull(message = "A quantidade é obrigatória")
    @DecimalMin(value = "0.0", message = "A quantidade não pode ser negativa")
    @Column(nullable = false)
    var quantidade: Int,
    
    @Column(name = "data_criacao")
    val dataCriacao: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "data_atualizacao")
    var dataAtualizacao: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        dataAtualizacao = LocalDateTime.now()
    }
}
