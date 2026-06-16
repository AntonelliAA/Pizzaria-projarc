package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ClientesRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

/**
 * Serviço de domínio "Cadastro de Clientes".
 * Os casos de uso acessam os dados de cliente sempre por aqui — nunca pelo repositório direto.
 */
@Service
public class ClienteService {
    private final ClientesRepository clientesRepository;

    @Autowired
    public ClienteService(ClientesRepository clientesRepository) {
        this.clientesRepository = clientesRepository;
    }

    public Optional<Cliente> recuperaPorCpf(String cpf) {
        return clientesRepository.recuperaPorCpf(cpf);
    }

    public Optional<Cliente> recuperaPorEmail(String email) {
        return clientesRepository.recuperaPorEmail(email);
    }

    public Cliente salva(Cliente cliente) {
        return clientesRepository.salva(cliente);
    }
}
