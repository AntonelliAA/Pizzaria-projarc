package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ValidacaoCredenciaisResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ClienteService;

/**
 * UC de validação de credenciais (a emissão de token foi para o gateway).
 * Confere email/senha e devolve a identidade (cpf) do cliente.
 */
@Service
@Transactional
public class AutenticarUC {

    private final ClienteService clienteService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AutenticarUC(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public ValidacaoCredenciaisResponse run(LoginRequest req) {
        Cliente c = clienteService.recuperaPorEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        String stored = c.getSenha();
        boolean ok = (stored != null && stored.startsWith("$2"))
                ? passwordEncoder.matches(req.getSenha(), stored)
                : req.getSenha().equals(stored);

        if (!ok) throw new IllegalArgumentException("Credenciais inválidas");

        return new ValidacaoCredenciaisResponse(c.getCpf(), c.getEmail());
    }
}
