package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

@Repository
public interface ProdutosRepository extends JpaRepository<Produto, Long>, com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository {

    @Override
    default Produto recuperaProdutoPorid(long id) {
        return findById(id).orElse(null);
    }

    @Override
    @Query("select distinct p from Cardapio c join c.produtos p where c.id = :cardapioId")
    List<Produto> recuperaProdutosCardapio(@Param("cardapioId") long id);
}
