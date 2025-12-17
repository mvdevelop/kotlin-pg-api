
package com.exemplo.api.service

import com.exemplo.api.dto.ProdutoDTO
import com.exemplo.api.dto.ProdutoResponseDTO
import com.exemplo.api.exception.ResourceNotFoundException
import com.exemplo.api.model.Produto
import com.exemplo.api.repository.ProdutoRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal  // Adicionar import

@Service
class ProdutoService(private val produtoRepository: ProdutoRepository) {

    @Transactional
    fun criarProduto(dto: ProdutoDTO): ProdutoResponseDTO {
        val produto = Produto(
            nome = dto.nome,
            descricao = dto.descricao,
            preco = dto.preco,
            quantidade = dto.quantidade
        )
        
        val produtoSalvo = produtoRepository.save(produto)
        return produtoSalvo.toResponseDTO()
    }

    fun listarTodos(): List<ProdutoResponseDTO> {
        return produtoRepository.findAll()
            .map { it.toResponseDTO() }
    }

    fun listarTodosPaginados(pageable: Pageable): Page<ProdutoResponseDTO> {
        return produtoRepository.findAll(pageable)
            .map { it.toResponseDTO() }
    }

    fun buscarPorId(id: Long): ProdutoResponseDTO {
        val produto = produtoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Produto não encontrado com ID: $id") }
        return produto.toResponseDTO()
    }

    @Transactional
    fun atualizarProduto(id: Long, dto: ProdutoDTO): ProdutoResponseDTO {
        val produto = produtoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Produto não encontrado com ID: $id") }
        
        produto.nome = dto.nome
        produto.descricao = dto.descricao
        produto.preco = dto.preco
        produto.quantidade = dto.quantidade
        
        val produtoAtualizado = produtoRepository.save(produto)
        return produtoAtualizado.toResponseDTO()
    }

    @Transactional
    fun deletarProduto(id: Long) {
        if (!produtoRepository.existsById(id)) {
            throw ResourceNotFoundException("Produto não encontrado com ID: $id")
        }
        produtoRepository.deleteById(id)
    }

    fun buscarPorNome(nome: String): List<ProdutoResponseDTO> {
        return produtoRepository.findByNomeContainingIgnoreCase(nome)
            .map { it.toResponseDTO() }
    }

    fun buscarPorFaixaDePreco(minPrice: BigDecimal, maxPrice: BigDecimal): List<ProdutoResponseDTO> {
        return produtoRepository.findByPriceRange(minPrice, maxPrice)
            .map { it.toResponseDTO() }
    }

    fun buscarProdutoMaisCaro(): ProdutoResponseDTO? {
        return produtoRepository.findFirstByOrderByPrecoDesc()
            .map { it.toResponseDTO() }
            .orElse(null)
    }

    fun calcularMediaPrecos(): BigDecimal? {
        return produtoRepository.findAveragePrice()
    }

    private fun Produto.toResponseDTO(): ProdutoResponseDTO {
        return ProdutoResponseDTO(
            id = this.id!!,
            nome = this.nome,
            descricao = this.descricao,
            preco = this.preco,
            quantidade = this.quantidade,
            dataCriacao = this.dataCriacao,
            dataAtualizacao = this.dataAtualizacao
        )
    }
}
