package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.CabecalhoCardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

@Repository
public interface CardapioRepository extends JpaRepository<Cardapio, Long>, com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.CardapioRepository {

    @Override
    @EntityGraph(attributePaths = {"produtos", "produtos.receita", "produtos.receita.ingredientes"})
    Optional<Cardapio> findById(Long id);

    @Override
    default Cardapio recuperaPorId(long id) {
        return findById(id).orElse(null);
    }

    @Override
    default List<CabecalhoCardapio> cardapiosDisponiveis() {
        return findAll().stream()
            .map(cardapio -> new CabecalhoCardapio(cardapio.getId(), cardapio.getTitulo()))
            .toList();
    }

    @Override
    default List<Produto> indicacoesDoChef() {
        return findById(1L)
            .map(cardapio -> cardapio.getProdutos().isEmpty() ? List.<Produto>of() : List.of(cardapio.getProdutos().getFirst()))
            .orElse(List.of());
    }
}