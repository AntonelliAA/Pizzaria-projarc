package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Ingrediente;

@Repository
public interface IngredientesRepository extends JpaRepository<Ingrediente, Long>, com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.IngredientesRepository {

    @Override
    default List<Ingrediente> recuperaTodos() {
        return findAll();
    }

    @Override
    @Query("select i from Receita r join r.ingredientes i where r.id = :receitaId")
    List<Ingrediente> recuperaIngredientesReceita(@Param("receitaId") long id);
}