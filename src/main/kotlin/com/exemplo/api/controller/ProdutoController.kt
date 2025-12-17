
package com.exemplo.api.controller

import com.exemplo.api.dto.ProdutoDTO
import com.exemplo.api.dto.ProdutoResponseDTO
import com.exemplo.api.service.ProdutoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal  // Adicionar import

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "API para gerenciamento de produtos")
class ProdutoController(
    private val produtoService: ProdutoService
) {

    @PostMapping
    @Operation(summary = "Criar um novo produto", description = "Cria um novo produto no sistema")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    )
    fun criarProduto(@Valid @RequestBody dto: ProdutoDTO): ResponseEntity<ProdutoResponseDTO> {
        val produto = produtoService.criarProduto(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(produto)
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista de todos os produtos cadastrados")
    fun listarTodos(): ResponseEntity<List<ProdutoResponseDTO>> {
        val produtos = produtoService.listarTodos()
        return ResponseEntity.ok(produtos)
    }

    @GetMapping("/paginados")
    @Operation(summary = "Listar produtos paginados", description = "Retorna uma página de produtos")
    fun listarTodosPaginados(pageable: Pageable): ResponseEntity<Page<ProdutoResponseDTO>> {
        val produtos = produtoService.listarTodosPaginados(pageable)
        return ResponseEntity.ok(produtos)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico pelo seu ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Produto encontrado"),
        ApiResponse(responseCode = "404", description = "Produto não encontrado")
    )
    fun buscarPorId(@PathVariable id: Long): ResponseEntity<ProdutoResponseDTO> {
        val produto = produtoService.buscarPorId(id)
        return ResponseEntity.ok(produto)
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar produtos por nome", description = "Busca produtos cujo nome contenha o termo fornecido")
    fun buscarPorNome(@RequestParam nome: String): ResponseEntity<List<ProdutoResponseDTO>> {
        val produtos = produtoService.buscarPorNome(nome)
        return ResponseEntity.ok(produtos)
    }

    @GetMapping("/faixa-preco")
    @Operation(summary = "Buscar produtos por faixa de preço", description = "Busca produtos dentro de uma faixa de preço")
    fun buscarPorFaixaDePreco(
        @RequestParam minPrice: BigDecimal,
        @RequestParam maxPrice: BigDecimal
    ): ResponseEntity<List<ProdutoResponseDTO>> {
        val produtos = produtoService.buscarPorFaixaDePreco(minPrice, maxPrice)
        return ResponseEntity.ok(produtos)
    }

    @GetMapping("/mais-caro")
    @Operation(summary = "Buscar produto mais caro", description = "Retorna o produto com o maior preço")
    fun buscarProdutoMaisCaro(): ResponseEntity<ProdutoResponseDTO?> {
        val produto = produtoService.buscarProdutoMaisCaro()
        return ResponseEntity.ok(produto)
    }

    @GetMapping("/media-precos")
    @Operation(summary = "Calcular média de preços", description = "Calcula a média de preços de todos os produtos")
    fun calcularMediaPrecos(): ResponseEntity<BigDecimal?> {
        val media = produtoService.calcularMediaPrecos()
        return ResponseEntity.ok(media)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
        ApiResponse(responseCode = "404", description = "Produto não encontrado")
    )
    fun atualizarProduto(
        @PathVariable id: Long,
        @Valid @RequestBody dto: ProdutoDTO
    ): ResponseEntity<ProdutoResponseDTO> {
        val produto = produtoService.atualizarProduto(id, dto)
        return ResponseEntity.ok(produto)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar produto", description = "Remove um produto do sistema")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
        ApiResponse(responseCode = "404", description = "Produto não encontrado")
    )
    fun deletarProduto(@PathVariable id: Long): ResponseEntity<Void> {
        produtoService.deletarProduto(id)
        return ResponseEntity.noContent().build()
    }
}
