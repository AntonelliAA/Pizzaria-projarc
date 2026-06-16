package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Servicos;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.IAuthTokenService;

/**
 * Implementação do serviço de autenticação baseada em tokens mantidos em memória.
 * Adaptador de infraestrutura — trocável pela implementação definitiva via {@link IAuthTokenService}.
 */
@Service
public class AuthTokenService implements IAuthTokenService {
    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    @Override
    public String createToken(String cpf) {
        String t = UUID.randomUUID().toString();
        tokens.put(t, new TokenInfo(cpf, Instant.now().plus(Duration.ofHours(8))));
        return t;
    }

    @Override
    public boolean validate(String token) {
        TokenInfo info = tokens.get(token);
        if (info == null) return false;
        if (Instant.now().isAfter(info.expiresAt)) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    @Override
    public String cpfFor(String token) {
        TokenInfo info = tokens.get(token);
        return info == null ? null : info.cpf;
    }

    private static class TokenInfo {
        final String cpf;
        final Instant expiresAt;
        TokenInfo(String cpf, Instant expiresAt) { this.cpf = cpf; this.expiresAt = expiresAt; }
    }
}
