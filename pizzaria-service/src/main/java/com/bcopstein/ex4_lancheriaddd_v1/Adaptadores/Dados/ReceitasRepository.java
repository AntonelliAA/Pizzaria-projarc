package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Receita;

@Repository
public interface ReceitasRepository extends JpaRepository<Receita, Long>, com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ReceitasRepository {

    @Override
    @EntityGraph(attributePaths = {"ingredientes"})
    Optional<Receita> findById(Long id);

    @Override
    default Receita recuperaReceita(long id) {
        return findById(id).orElse(null);
    }
}
