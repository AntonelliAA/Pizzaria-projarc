package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.LoginRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.AuthResponse;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ClientesRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

@Service
@Transactional
public class AutenticarUC {

    private final ClientesRepository clientesRepo;
    private final AuthTokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AutenticarUC(ClientesRepository clientesRepo, AuthTokenService tokenService) {
        this.clientesRepo = clientesRepo;
        this.tokenService = tokenService;
    }

    public AuthResponse run(LoginRequest req) {
        Optional<Cliente> oc = clientesRepo.recuperaPorEmail(req.getEmail());
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
