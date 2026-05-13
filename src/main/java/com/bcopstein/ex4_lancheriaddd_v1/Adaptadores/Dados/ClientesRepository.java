package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

@Repository
public interface ClientesRepository extends JpaRepository<Cliente, String>, com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ClientesRepository {

    @Override
    default Optional<Cliente> recuperaPorCpf(String cpf) {
        return findById(cpf);
    }
}
