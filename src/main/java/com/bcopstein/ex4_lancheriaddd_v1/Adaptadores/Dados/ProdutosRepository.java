package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

@Repository
public interface ProdutosRepository extends JpaRepository<Produto, Long>,
        com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository {

    @Override
    default Produto recuperaProdutoPorid(long id) {
        return findById(id).orElse(null);
    }

    @Override
    @Query("SELECT DISTINCT p FROM Cardapio c JOIN c.produtos p WHERE c.id = :cardapioId")
    List<Produto> recuperaProdutosCardapio(@Param("cardapioId") long id);

    @Modifying
    @Query("UPDATE Produto p SET p.disponivel = false WHERE p.id = :id")
    void marcaIndisponivelJpa(@Param("id") long id);

    @Override
    default void marcaComoIndisponivel(long id) {
        marcaIndisponivelJpa(id);
    }
}
