package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.AuthResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ClienteService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IAuthTokenService;

@Service
@Transactional
public class AutenticarUC {

    private final ClienteService clienteService;
    private final IAuthTokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AutenticarUC(ClienteService clienteService, IAuthTokenService tokenService) {
        this.clienteService = clienteService;
        this.tokenService = tokenService;
    }

    public AuthResponse run(LoginRequest req) {
        Optional<Cliente> oc = clienteService.recuperaPorEmail(req.getEmail());
        Cliente c = oc.orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        String stored = c.getSenha();
        boolean ok = false;
        if (stored != null && stored.startsWith("$2")) {
            ok = passwordEncoder.matches(req.getSenha(), stored);
        } else {
            ok = req.getSenha().equals(stored);
        }

        if (!ok) throw new IllegalArgumentException("Credenciais inválidas");

        String token = tokenService.createToken(c.getCpf());
        return new AuthResponse(token, c.getCpf(), c.getEmail());
    }
}
