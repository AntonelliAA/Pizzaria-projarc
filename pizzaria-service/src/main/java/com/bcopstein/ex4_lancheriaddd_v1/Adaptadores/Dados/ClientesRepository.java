package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

@Repository
public interface ClientesRepository extends JpaRepository<Cliente, String>,
        com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ClientesRepository {

    Optional<Cliente> findByEmail(String email);

    @Override
    default Optional<Cliente> recuperaPorCpf(String cpf) {
        return findById(cpf);
    }

    @Override
    default Optional<Cliente> recuperaPorEmail(String email) {
        return findByEmail(email);
    }

    @Override
    default Cliente salva(Cliente c) {
        return save(c);
    }
}
