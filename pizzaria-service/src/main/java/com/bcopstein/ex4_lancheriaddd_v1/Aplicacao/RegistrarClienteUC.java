package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.ClienteRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ClienteService;

@Service
@Transactional
public class RegistrarClienteUC {

    private final ClienteService clienteService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    public RegistrarClienteUC(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public Cliente run(ClienteRequest req) {
        if (clienteService.recuperaPorCpf(req.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado: " + req.getCpf());
        }

        String hashed = passwordEncoder.encode(req.getSenha());
        Cliente c = new Cliente(req.getCpf(), req.getNome(), req.getCelular(), req.getEndereco(), req.getEmail(), hashed);
        return clienteService.salva(c);
    }
}
