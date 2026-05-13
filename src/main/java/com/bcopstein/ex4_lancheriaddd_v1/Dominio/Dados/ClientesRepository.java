package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import java.util.Optional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

public interface ClientesRepository {
    Optional<Cliente> recuperaPorCpf(String cpf);
}
